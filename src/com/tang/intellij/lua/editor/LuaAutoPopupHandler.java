package com.tang.intellij.lua.editor;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
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
        if (!(file instanceof LuaFile)) return Result.CONTINUE;

        if (charTyped == ':' || charTyped == '.' || charTyped == ' ' || charTyped == '@') {
            AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
            return Result.STOP;
        }

        return Result.CONTINUE;
    }
}
