package com.tang.intellij.lua.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.psi.LuaLocalFuncDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tangzx
 * Date : 2015/11/16.
 */
public class LuaFoldingBuilder extends FoldingBuilderEx {
    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement psiElement, @NotNull Document document, boolean b) {
        List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
        Collection<LuaLocalFuncDef> localFuncDefs = PsiTreeUtil.findChildrenOfType(psiElement, LuaLocalFuncDef.class);
        for (final LuaLocalFuncDef funcDef : localFuncDefs) {
            descriptors.add(new FoldingDescriptor(funcDef, funcDef.getTextRange()) {
                @Nullable
                @Override
                public String getPlaceholderText() {
                    return "local function() ... end";
                }
            });
        }
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode astNode) {
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return true;
    }
}
