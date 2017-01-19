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

package com.tang.intellij.lua.codeInsight.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaFuncBodyOwner;
import com.tang.intellij.lua.psi.LuaParamNameDef;
import com.tang.intellij.lua.psi.LuaParametersOwner;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/12.
 */
public class CreateParamDocIntention extends BaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @NotNull
    @Override
    public String getText() {
        return "Create param doc";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element != null) {
            element = element.getParent();
            if (element instanceof LuaParamNameDef) {
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
        LuaParamNameDef parDef = (LuaParamNameDef) element.getParent();
        LuaParametersOwner parametersOwner = PsiTreeUtil.getParentOfType(parDef, LuaParametersOwner.class);
        if (parametersOwner != null && parametersOwner instanceof LuaCommentOwner) {
            LuaComment comment = ((LuaCommentOwner) parametersOwner).getComment();

            TemplateManager templateManager = TemplateManager.getInstance(project);
            Template template = templateManager.createTemplate("", "");
            if (comment != null) template.addTextSegment("\n");
            template.addTextSegment(String.format("---@param %s ", parDef.getName()));
            MacroCallNode name = new MacroCallNode(new SuggestTypeMacro());
            template.addVariable("type", name, new TextExpression("table"), true);
            template.addEndVariable();

            if (comment != null) {
                editor.getCaretModel().moveToOffset(comment.getTextOffset() + comment.getTextLength());
            } else {
                editor.getCaretModel().moveToOffset(parametersOwner.getNode().getStartOffset());
                template.addTextSegment("\n");
            }

            templateManager.startTemplate(editor, template);
        }
    }
}
