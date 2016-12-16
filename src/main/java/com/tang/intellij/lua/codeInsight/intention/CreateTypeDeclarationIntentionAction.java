package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaLocalDef;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/16.
 */
public class CreateTypeDeclarationIntentionAction extends BaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "family";
    }

    @NotNull
    @Override
    public String getText() {
        return "Create type declaration";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        LuaLocalDef localDef = PsiTreeUtil.getParentOfType(element, LuaLocalDef.class);
        if (localDef != null) {
            LuaComment comment = localDef.getComment();
            return comment == null || comment.getTypeDef() == null;
        }
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        LuaLocalDef localDef = PsiTreeUtil.getParentOfType(element, LuaLocalDef.class);
        if (localDef != null) {
            LuaComment comment = localDef.getComment();
            if (comment == null) {
                editor.getDocument().insertString(localDef.getTextOffset(), "---@type #table\n");
            } else {
                editor.getDocument().insertString(comment.getTextOffset(), "---@type #table\n");
            }
        }
    }
}
