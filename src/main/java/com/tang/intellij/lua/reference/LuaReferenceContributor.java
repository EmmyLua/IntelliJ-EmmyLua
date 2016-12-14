package com.tang.intellij.lua.reference;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.psi.LuaCallExpr;
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
    }

    class CallExprReferenceProvider extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
            LuaCallExpr expr = (LuaCallExpr) psiElement;
            if (expr.getId() == null)
                return PsiReference.EMPTY_ARRAY;
            return new PsiReference[]{ new LuaCallExprReference(expr) };
        }
    }
}
