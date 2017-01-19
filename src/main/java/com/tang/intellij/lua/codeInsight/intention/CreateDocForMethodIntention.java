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

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaFuncBody;
import com.tang.intellij.lua.psi.LuaParamNameDef;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/16.
 */
public class CreateDocForMethodIntention extends ClassMethodBasedIntention {
    @Override
    protected boolean isAvailable(LuaClassMethodDef methodDef, Editor editor) {
        return methodDef.getComment() == null || methodDef.getFuncBody() == null;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @NotNull
    @Override
    public String getText() {
        return "Create doc";
    }

    @Override
    protected void invoke(LuaClassMethodDef methodDef, Editor editor) {
        LuaFuncBody funcBody = methodDef.getFuncBody();
        if (funcBody != null) {
            TemplateManager templateManager = TemplateManager.getInstance(methodDef.getProject());
            Template template = templateManager.createTemplate("", "");
            template.addTextSegment("---" + methodDef.getName());
            MacroCallNode typeSuggest = new MacroCallNode(new SuggestTypeMacro());

            // params
            List<LuaParamNameDef> parDefList = funcBody.getParamNameDefList();
            for (LuaParamNameDef parDef : parDefList) {
                template.addTextSegment(String.format("\n---@param %s ", parDef.getName()));
                template.addVariable(parDef.getName(), typeSuggest, new TextExpression("table"), false);
            }

            template.addEndVariable();
            template.addTextSegment("\n");

            int textOffset = methodDef.getNode().getStartOffset();
            editor.getCaretModel().moveToOffset(textOffset);
            templateManager.startTemplate(editor, template);
        }
    }
}
