package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.psi.LuaParDef;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/12.
 */
public class CreateParamDocAction extends BaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "family name";
    }

    @NotNull
    @Override
    public String getText() {
        return "Create Param Doc";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset - 1);
        if (element != null) {
            element = element.getParent();
            if (element instanceof LuaParDef) {
                //TODO: 并且没有相应 Doc
                return true;
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {

    }
}
