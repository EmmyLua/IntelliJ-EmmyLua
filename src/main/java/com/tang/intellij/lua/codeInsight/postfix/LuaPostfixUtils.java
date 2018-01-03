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

package com.tang.intellij.lua.codeInsight.postfix;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelector;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelectorBase;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.tang.intellij.lua.psi.LuaExpr;
import com.tang.intellij.lua.psi.LuaExprStat;
import com.tang.intellij.lua.psi.LuaParenExpr;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LuaPostfixUtils {

    public static final Condition<PsiElement> IS_NON_PAR = (element) -> element instanceof LuaExpr && !(element instanceof LuaParenExpr);

    public static PostfixTemplateExpressionSelector selectorTopmost() {
        return selectorTopmost(Conditions.alwaysTrue());
    }

    public static PostfixTemplateExpressionSelector selectorTopmost(Condition<PsiElement> additionalFilter) {
        return new PostfixTemplateExpressionSelectorBase(additionalFilter) {
            @Override
            protected List<PsiElement> getNonFilteredExpressions(@NotNull PsiElement psiElement, @NotNull Document document, int i) {
                LuaExprStat stat = PsiTreeUtil.getNonStrictParentOfType(psiElement, LuaExprStat.class);
                PsiElement expr = null;
                if (stat != null) {
                    expr = stat.getExpr();
                }

                return ContainerUtil.createMaybeSingletonList(expr);
            }
        };
    }

    public static PostfixTemplateExpressionSelector selectorAllExpressionsWithCurrentOffset() {
        return selectorAllExpressionsWithCurrentOffset(Conditions.alwaysTrue());
    }

    public static PostfixTemplateExpressionSelector selectorAllExpressionsWithCurrentOffset(final Condition<PsiElement> additionalFilter) {
        return new PostfixTemplateExpressionSelectorBase(additionalFilter) {
            @Override
            protected List<PsiElement> getNonFilteredExpressions(@NotNull PsiElement psiElement, @NotNull Document document, int i) {
                LuaExpr expr = PsiTreeUtil.getNonStrictParentOfType(psiElement, LuaExpr.class);
                List<PsiElement> list = new SmartList<>();
                while (expr != null) {
                    if (!PsiTreeUtil.hasErrorElements(expr))
                        list.add(expr);
                    expr = PsiTreeUtil.getParentOfType(expr, LuaExpr.class);
                }
                return list;
            }
        };
    }
}
