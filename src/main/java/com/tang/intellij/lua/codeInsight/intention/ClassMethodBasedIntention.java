package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/16.
 */
public abstract class ClassMethodBasedIntention extends BaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "ClassMethodBasedIntention";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        LuaClassMethodDef classMethodDef = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.getCaretModel().getOffset(), LuaClassMethodDef.class, false);
        return classMethodDef != null && isAvailable(classMethodDef, editor);
    }

    protected boolean isAvailable(LuaClassMethodDef methodDef, Editor editor) {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        LuaClassMethodDef classMethodDef = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.getCaretModel().getOffset(), LuaClassMethodDef.class, false);
        invoke(classMethodDef, editor);
    }

    protected void invoke(LuaClassMethodDef methodDef, Editor editor) {

    }
}
