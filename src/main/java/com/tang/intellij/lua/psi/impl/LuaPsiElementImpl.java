package com.tang.intellij.lua.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.tang.intellij.lua.psi.LuaPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * LuaPsiElement 基类
 * Created by TangZX on 2016/11/24.
 */
public class LuaPsiElementImpl extends ASTWrapperPsiElement implements LuaPsiElement {
    LuaPsiElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this, PsiReferenceService.Hints.NO_HINTS);
    }
}
