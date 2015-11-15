package com.tang.intellij.lua.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by tangzx
 * Date : 2015/11/16.
 */
public class LuaFoldingBuilder extends CustomFoldingBuilder {
    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> list,
                                            @NotNull PsiElement psiElement,
                                            @NotNull Document document, boolean b) {
        //if (!(psiElement instanceof LuaFile)) return;
    }

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode astNode, @NotNull TextRange textRange) {
        return null;
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode astNode) {
        return false;
    }
}
