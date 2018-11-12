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

package com.tang.intellij.lua.codeInsight.editorActions

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.psi.LuaTypes
import java.awt.datatransfer.DataFlavor

class StringLiteralPasteProvider : PasteProvider {
    override fun isPastePossible(dataContext: DataContext): Boolean {
        return findRawStringLiteralAtCaret(dataContext) != null && getClipText() != null
    }

    override fun performPaste(dataContext: DataContext) {
        val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return
        val literalExpr = findRawStringLiteralAtCaret(dataContext) ?: return
        val clipText = getClipText() ?: return

        val text = literalExpr.text
        val content = LuaString.getContent(text)
        val quotes = text.substring(0, content.start)
        var value = clipText
        if (quotes == "'") {
            value = value.replace("\'", "\\\'")
        } else if (quotes == "\"") {
            value = value.replace("\"", "\\\"")
        }

        WriteCommandAction.runWriteCommandAction(editor.project) {
            EditorModificationUtil.insertStringAtCaret(editor, value)
        }
    }

    override fun isPasteEnabled(dataContext: DataContext): Boolean {
        return isPastePossible(dataContext)
    }

    private fun findRawStringLiteralAtCaret(dataContext: DataContext): PsiElement? {
        val editor = dataContext.getData(CommonDataKeys.EDITOR)
        val file = dataContext.getData(CommonDataKeys.PSI_FILE)
        if (editor != null && file != null) {
            val element = file.findElementAt(editor.caretModel.offset)
            if (element != null && element.node.elementType == LuaTypes.STRING)
                return element
        }
        return null
    }

    private fun getClipText(): String? {
        return CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor)
    }
}