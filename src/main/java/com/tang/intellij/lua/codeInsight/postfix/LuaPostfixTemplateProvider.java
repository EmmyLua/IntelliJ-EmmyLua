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

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import com.tang.intellij.lua.codeInsight.postfix.templates.*;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 *
 * Created by tangzx on 2017/2/4.
 */
public class LuaPostfixTemplateProvider implements PostfixTemplateProvider {

    private Set<PostfixTemplate> templates;

    public LuaPostfixTemplateProvider() {
        templates = ContainerUtil.newHashSet(
                new LuaLocalPostfixTemplate(),
                new LuaForAPostfixTemplate(),
                new LuaForIPostfixTemplate(),
                new LuaForPPostfixTemplate(),
                new LuaIfPostfixTemplate(),
                new LuaIfNotPostfixTemplate(),
                new LuaCheckNilPostfixTemplate(),
                new LuaCheckIfNotNilPostfixTemplate(),
                new LuaReturnPostfixTemplate(),
                new LuaPrintPostfixTemplate(),
                new LuaIncreasePostfixTemplate(),
                new LuaDecreasePostfixTemplate(),
                new LuaParPostfixTemplate(),
                new LuaToNumberPostfixTemplate(),
                new LuaToStringPostfixTemplate()
        );
    }

    @NotNull
    @Override
    public Set<PostfixTemplate> getTemplates() {
        return templates;
    }

    @Override
    public boolean isTerminalSymbol(char c) {
        return c == '.';
    }

    @Override
    public void preExpand(@NotNull PsiFile psiFile, @NotNull Editor editor) {

    }

    @Override
    public void afterExpand(@NotNull PsiFile psiFile, @NotNull Editor editor) {

    }

    @NotNull
    @Override
    public PsiFile preCheck(@NotNull PsiFile psiFile, @NotNull Editor editor, int i) {
        return psiFile;
    }
}
