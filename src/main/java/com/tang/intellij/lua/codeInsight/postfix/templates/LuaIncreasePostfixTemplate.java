/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tang.intellij.lua.codeInsight.postfix.templates;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.postfix.templates.StringBasedPostfixTemplate;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.tang.intellij.lua.codeInsight.postfix.LuaPostfixUtils.selectorTopmost;

/**
 *
 * Created by TangZX on 2017/2/7.
 */
public class LuaIncreasePostfixTemplate extends StringBasedPostfixTemplate {
    public LuaIncreasePostfixTemplate() {
        super("increase", "expr = expr + value", selectorTopmost());
    }

    @Nullable
    @Override
    public String getTemplateString(@NotNull PsiElement psiElement) {
        return "$expr$ = $expr$ + $value$";
    }

    @Override
    protected PsiElement getElementToRemove(PsiElement expr) {
        return expr;
    }

    @Override
    public void setVariables(@NotNull Template template, @NotNull PsiElement element) {
        super.setVariables(template, element);
        template.addVariable("value", new TextExpression("1"), true);
    }
}
