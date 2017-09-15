/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.editor.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.LuaIndentRange
import com.tang.intellij.lua.psi.LuaTypes.*

/**
 * 关键字插入时缩进处理
 * Created by TangZX on 2016/12/20.
 */
class KeywordInsertHandler internal constructor(private val keyWordToken: IElementType) : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        val file = insertionContext.file
        val project = insertionContext.project
        val document = insertionContext.document
        val offset = insertionContext.tailOffset
        val element = file.findElementAt(offset)
        if (element != null && element !is PsiWhiteSpace) {
            document.insertString(insertionContext.tailOffset, " ")
            insertionContext.editor.caretModel.moveToOffset(insertionContext.tailOffset)
        }
        autoIndent(keyWordToken, file, project, document, offset)
    }

    companion object {

        fun autoIndent(keyWordToken: IElementType, file: PsiFile, project: Project, document: Document, offset: Int) {
            if (keyWordToken === END || keyWordToken === ELSE || keyWordToken === ELSEIF) {
                PsiDocumentManager.getInstance(project).commitDocument(document)
                val element = PsiTreeUtil.findElementOfClassAtOffset(file, offset, LuaIndentRange::class.java, false)
                if (element != null) {
                    val styleManager = CodeStyleManager.getInstance(project)
                    styleManager.adjustLineIndent(file, element.textRange)
                }
            }
        }
    }
}
