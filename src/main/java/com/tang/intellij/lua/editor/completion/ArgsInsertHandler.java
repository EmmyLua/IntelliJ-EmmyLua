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

package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.macro.SuggestVariableNameMacro;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.psi.LuaTypes;

public abstract class ArgsInsertHandler implements InsertHandler<LookupElement> {

    protected abstract String[] getParams();

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        int startOffset = insertionContext.getStartOffset();
        PsiElement element = insertionContext.getFile().findElementAt(startOffset);
        if (element != null) {
            element = element.getNextSibling();
            while (element instanceof PsiWhiteSpace) {
                element = element.getNextSibling();
            }
            if (element != null) {
                IElementType type = element.getNode().getElementType();
                if (type == LuaTypes.LPAREN || type == LuaTypes.ARGS) {
                    return;
                }
            }
        }

        String[] paramNameDefList = getParams();
        if (paramNameDefList != null) {
            TemplateManager manager = TemplateManager.getInstance(insertionContext.getProject());
            Template template = createTemplate(manager, paramNameDefList);
            insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getSelectionEndOffset());
            manager.startTemplate(insertionContext.getEditor(), template);
        }
    }

    Template createTemplate(TemplateManager manager, String[] paramNameDefList) {
        Template template = manager.createTemplate("", "");
        template.addTextSegment("(");

        boolean isFirst = true;
        MacroCallNode name = new MacroCallNode(new SuggestVariableNameMacro());

        for (String paramNameDef : paramNameDefList) {
            if (!isFirst)
                template.addTextSegment(", ");
            template.addVariable(paramNameDef, name, new TextExpression(paramNameDef), false);
            isFirst = false;
        }
        template.addTextSegment(")");
        return template;
    }
}