package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.psi.LuaFuncBodyOwner;
import com.tang.intellij.lua.psi.LuaParDef;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/12.
 */
public class CreateParamDocIntentionAction extends BaseIntentionAction {
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
        PsiElement element = psiFile.findElementAt(offset);
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
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        assert element != null;
        LuaParDef parDef = (LuaParDef) element.getParent();
        LuaFuncBodyOwner bodyOwner = PsiTreeUtil.getParentOfType(parDef, LuaFuncBodyOwner.class);
        if (bodyOwner != null && bodyOwner instanceof LuaCommentOwner) {
            LuaComment comment = ((LuaCommentOwner) bodyOwner).getComment();
            if (comment != null) {
                comment.add(LuaElementFactory.createLineBreak(project));
                comment.add(LuaElementFactory.createParamDoc(project, parDef.getName()));
            } else {
                String doc = String.format("---@param %s #table\n", parDef.getName());
                editor.getDocument().insertString(bodyOwner.getTextOffset(), doc);
            }
        }
    }
}
