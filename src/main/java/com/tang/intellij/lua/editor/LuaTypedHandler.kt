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

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.LuaFuncBody
import com.tang.intellij.lua.psi.LuaTypes

/**

 * Created by TangZX on 2016/11/28.
 */
class LuaTypedHandler : TypedHandlerDelegate() {

    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): TypedHandlerDelegate.Result {
        if (file.fileType == LuaFileType.INSTANCE) {
            if (charTyped == ':' || charTyped == '@') {
                AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null)
                return TypedHandlerDelegate.Result.STOP
            }
            if (charTyped == '.') {
                val element = file.findElementAt(editor.caretModel.offset - 1)
                when (element?.node?.elementType) {
                    LuaTypes.DOT,
                    LuaTypes.SHORT_COMMENT -> return TypedHandlerDelegate.Result.STOP
                }
            }
        }
        return super.checkAutoPopup(charTyped, project, editor, file)
    }

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): TypedHandlerDelegate.Result {
        if (file.fileType == LuaFileType.INSTANCE) {
            // function() <caret> end 自动加上end
            if (c == '(') {
                PsiDocumentManager.getInstance(project).commitDocument(editor.document)
                val pos = editor.caretModel.offset
                val element = file.findElementAt(pos)
                if (element != null && element.parent is LuaFuncBody) {
                    editor.document.insertString(pos + 1, " end")
                    return TypedHandlerDelegate.Result.STOP
                }
            }
        }
        return super.charTyped(c, project, editor, file)
    }
}
