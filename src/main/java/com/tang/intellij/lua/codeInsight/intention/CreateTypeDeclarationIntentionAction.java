package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.macro.SuggestVariableNameMacro;
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
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        LuaLocalDef localDef = PsiTreeUtil.getParentOfType(element, LuaLocalDef.class);
        if (localDef != null) {
            LuaComment comment = localDef.getComment();

            TemplateManager templateManager = TemplateManager.getInstance(project);
            Template template = templateManager.createTemplate("", "");
            if (comment != null) template.addTextSegment("\n");
            template.addTextSegment("---@type #");
            MacroCallNode name = new MacroCallNode(new SuggestVariableNameMacro());
            template.addVariable("type", name, new TextExpression("table"), true);

            if (comment != null) {
                editor.getCaretModel().moveToOffset(comment.getTextOffset() + comment.getTextLength());
            } else {
                editor.getCaretModel().moveToOffset(localDef.getTextOffset());
                template.addTextSegment("\n");
            }
            templateManager.startTemplate(editor, template);
        }
    }
}