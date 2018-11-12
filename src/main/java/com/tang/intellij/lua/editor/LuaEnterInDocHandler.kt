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

package com.tang.intellij.lua.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.psi.LuaDocTypes
import com.tang.intellij.lua.comment.psi.api.LuaComment

/**
 * LuaEnterInDocHandler
 * Created by tangzx on 2017/2/19.
 */
class LuaEnterInDocHandler : EnterHandlerDelegate {
    override fun preprocessEnter(psiFile: PsiFile,
                                 editor: Editor,
                                 caretOffsetRef: Ref<Int>,
                                 caretAdvance: Ref<Int>,
                                 dataContext: DataContext,
                                 editorActionHandler: EditorActionHandler?): EnterHandlerDelegate.Result? {
        val caretOffset = caretOffsetRef.get()

        //inside comment
        val comment = PsiTreeUtil.findElementOfClassAtOffset(psiFile, caretOffset - 1, LuaComment::class.java, false)
        if (comment != null && caretOffset > comment.textOffset) {
            val children = comment.node.getChildren(TokenSet.create(LuaDocTypes.DASHES))
            val last = children[children.size - 1]
            //在最后一个 --- 之前才有效
            if (caretOffset > last.startOffset && comment.owner == null)
                return null

            val document = editor.document

            document.insertString(caretOffset, "\n---")
            editor.caretModel.moveToOffset(caretOffset + 4)

            val project = comment.project
            val textRange = comment.textRange
            PsiDocumentManager.getInstance(project).commitDocument(document)
            ApplicationManager.getApplication().runWriteAction {
                val styleManager = CodeStyleManager.getInstance(project)
                styleManager.adjustLineIndent(psiFile, textRange)
            }
            return EnterHandlerDelegate.Result.Stop
        }
        return EnterHandlerDelegate.Result.Continue
    }

    override fun postProcessEnter(psiFile: PsiFile, editor: Editor, dataContext: DataContext): EnterHandlerDelegate.Result? {
        return null
    }
}
