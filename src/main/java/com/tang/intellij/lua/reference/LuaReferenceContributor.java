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

package com.tang.intellij.lua.reference;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.psi.LuaCallExpr;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaNameRef;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * reference contributor
 * Created by TangZX on 2016/12/14.
 */
public class LuaReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(psiElement().withElementType(LuaTypes.CALL_EXPR), new CallExprReferenceProvider());
        psiReferenceRegistrar.registerReferenceProvider(psiElement().withElementType(LuaTypes.INDEX_EXPR), new IndexExprReferenceProvider());
        psiReferenceRegistrar.registerReferenceProvider(psiElement().withElementType(LuaTypes.FUNC_PREFIX_REF), new IndexExprReferenceProvider());
        psiReferenceRegistrar.registerReferenceProvider(psiElement().withElementType(LuaTypes.NAME_REF), new NameReferenceProvider());
    }

    class CallExprReferenceProvider extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
            LuaCallExpr expr = (LuaCallExpr) psiElement;
            LuaNameRef nameRef = expr.getNameRef();
            if (nameRef != null) {
                if (nameRef.getText().equals("require")) {
                    return new PsiReference[] { new LuaRequireReference(expr) };
                }
            }

            PsiElement id = expr.getId();
            if (id == null)
                return PsiReference.EMPTY_ARRAY;
            return new PsiReference[]{ new LuaCallExprReference(expr) };
        }
    }

    class IndexExprReferenceProvider extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
            LuaIndexExpr indexExpr = (LuaIndexExpr) psiElement;
            PsiElement id = indexExpr.getId();
            if (id != null) {
                return new PsiReference[] { new LuaIndexReference(indexExpr, id) };
            }
            return PsiReference.EMPTY_ARRAY;
        }
    }

    class NameReferenceProvider extends PsiReferenceProvider {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
            return new PsiReference[] { new LuaNameReference((LuaNameRef) psiElement) };
        }
    }
}
