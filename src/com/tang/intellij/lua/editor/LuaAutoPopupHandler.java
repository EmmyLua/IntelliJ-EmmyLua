package com.tang.intellij.lua.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.tang.intellij.lua.psi.LuaFile;
import com.tang.intellij.lua.psi.LuaFuncBody;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/11/28.
 */
public class LuaAutoPopupHandler extends TypedHandlerDelegate {

    @Override
    public Result charTyped(char charTyped, Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!(file instanceof LuaFile)) return Result.CONTINUE;

        // function() <caret> end 自动加上end
        if (charTyped == ')') {
            int pos = editor.getCaretModel().getOffset();
            PsiElement element = file.findElementAt(pos - 1);
            if (element != null) {
                LuaFuncBody parent = (LuaFuncBody) element.getParent();
                if (parent != null) {
                    editor.getDocument().insertString(pos,"  end");
                    editor.getCaretModel().moveToOffset(pos + 1);
                }
            }
        }

        return Result.CONTINUE;
    }
}
