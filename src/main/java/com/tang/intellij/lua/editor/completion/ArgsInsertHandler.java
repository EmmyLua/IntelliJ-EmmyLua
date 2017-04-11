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
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.psi.LuaParamInfo;
import com.tang.intellij.lua.psi.LuaTypes;

public abstract class ArgsInsertHandler implements InsertHandler<LookupElement> {

    protected abstract LuaParamInfo[] getParams();

    protected int mask = -1;

    public ArgsInsertHandler withMask(int mask) {
        this.mask = mask;
        return this;
    }

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        int startOffset = insertionContext.getStartOffset();
        PsiElement element = insertionContext.getFile().findElementAt(startOffset);
        Editor editor = insertionContext.getEditor();
        if (element != null) {
            EditorEx ex = (EditorEx) editor;
            HighlighterIterator iterator = ex.getHighlighter().createIterator(startOffset);
            iterator.advance();
            IElementType tokenType = iterator.getTokenType();
            while (tokenType == TokenType.WHITE_SPACE) {
                iterator.advance();
                tokenType = iterator.getTokenType();
            }
            if (tokenType == LuaTypes.LPAREN)
                return;
        }

        LuaParamInfo[] paramNameDefList = getParams();
        if (paramNameDefList != null) {
            TemplateManager manager = TemplateManager.getInstance(insertionContext.getProject());
            Template template = createTemplate(manager, paramNameDefList);
            editor.getCaretModel().moveToOffset(insertionContext.getSelectionEndOffset());
            manager.startTemplate(editor, template);
        }
    }

    Template createTemplate(TemplateManager manager, LuaParamInfo[] paramNameDefList) {
        Template template = manager.createTemplate("", "");
        template.addTextSegment("(");

        boolean isFirst = true;

        for (int i = 0; i < paramNameDefList.length; i++) {
            if ((mask & (1 << i)) == 0) continue;

            LuaParamInfo paramNameDef = paramNameDefList[i];
            if (!isFirst)
                template.addTextSegment(", ");
            template.addVariable(paramNameDef.getName(), new TextExpression(paramNameDef.getName()), new TextExpression(paramNameDef.getName()), true);
            isFirst = false;
        }
        template.addTextSegment(")");
        return template;
    }
}