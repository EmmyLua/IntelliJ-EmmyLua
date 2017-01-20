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

package com.tang.intellij.lua.psi;

import com.intellij.codeInsight.TargetElementEvaluatorEx2;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/1/20.
 */
public class LuaTargetElementEvaluator extends TargetElementEvaluatorEx2 {
    @Nullable
    @Override
    public PsiElement getElementByReference(@NotNull PsiReference psiReference, int i) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement adjustTargetElement(Editor editor, int offset, int flags, @NotNull PsiElement targetElement) {
        if (targetElement instanceof LuaNameRef && !(targetElement.getParent() instanceof LuaVar)) {
            PsiElement element = LuaPsiResolveUtil.resolveLocal((LuaNameRef) targetElement, new SearchContext(editor.getProject()));
            return element == null ? null : targetElement;
        }
        return targetElement;
    }
}
