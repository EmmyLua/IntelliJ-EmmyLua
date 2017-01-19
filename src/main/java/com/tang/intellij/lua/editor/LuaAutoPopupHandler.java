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

package com.tang.intellij.lua.editor;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.tang.intellij.lua.psi.LuaFile;

/**
 *
 * Created by TangZX on 2016/11/28.
 */
public class LuaAutoPopupHandler extends TypedHandlerDelegate {

    @Override
    public Result checkAutoPopup(char charTyped, Project project, Editor editor, PsiFile file) {
        if (charTyped == ':' || charTyped == '@') {
            AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
            return Result.STOP;
        }
        return super.checkAutoPopup(charTyped, project, editor, file);
    }

    @Override
    public Result beforeCharTyped(char charTyped, Project project, Editor editor, PsiFile file, FileType fileType) {
        if (!(file instanceof LuaFile)) return Result.CONTINUE;

        // function() <caret> end 自动加上end
//        if (charTyped == '(') {
//            int pos = editor.getCaretModel().getOffset();
//            PsiElement element = file.findElementAt(pos - 1);
//            if (element != null && element.getParent() instanceof LuaFuncBody) {
//                editor.getDocument().insertString(pos,"  end");
//                editor.getCaretModel().moveToOffset(pos + 1);
//                return Result.STOP;
//            }
//        }

        return Result.CONTINUE;
    }
}
