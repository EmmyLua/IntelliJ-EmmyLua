package com.tang.intellij.lua.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.ElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.psi.LuaBlock;
import com.tang.intellij.lua.psi.LuaPsiTreeUtil;
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

    static final String HOLDER_TEXT = "...";

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement psiElement, @NotNull Document document, boolean b) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        Collection<LuaBlock> luaFuncBodies = PsiTreeUtil.findChildrenOfType(psiElement, LuaBlock.class);
        luaFuncBodies.forEach(block -> {
            PsiElement prev = PsiTreeUtil.skipSiblingsBackward(block, PsiWhiteSpace.class);
            PsiElement next = PsiTreeUtil.skipSiblingsForward(block, PsiWhiteSpace.class);
            if (prev != null && next != null) {
                int l = prev.getTextOffset() + prev.getTextLength();
                int r = next.getTextOffset();

                TextRange range = new TextRange(l, r);
                if (range.getLength() > 0 && !addOneLineMethodFolding(descriptors, block, range, prev, next)) {
                    descriptors.add(new FoldingDescriptor(block, range) {
                        @Nullable
                        @Override
                        public String getPlaceholderText() {
                            return HOLDER_TEXT;
                        }
                    });
                }
            }
        });
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private boolean addOneLineMethodFolding(List<FoldingDescriptor> descriptors, LuaBlock block, TextRange range, PsiElement prev, PsiElement next) {
        PsiElement[] children = block.getChildren();
        if (children.length == 0) return false;

        int validCount = 0;
        PsiElement first = null;
        for (PsiElement child : children) {
            if (!(child instanceof PsiWhiteSpace)) {
                first = child;
                validCount++;
                if (validCount > 1) return false;
            }
        }

        if (validCount == 1) {
            if (first.textContains('\n')) return false;

            FoldingGroup group = FoldingGroup.newGroup("one-liner");

            TextRange lRange = new TextRange(range.getStartOffset(), first.getTextOffset());
            descriptors.add(new NamedFoldingDescriptor(prev.getNode(), lRange, group, HOLDER_TEXT));

            TextRange rRange = new TextRange(first.getTextOffset() + first.getTextLength(), range.getEndOffset());
            descriptors.add(new NamedFoldingDescriptor(next.getNode(), rRange, group, HOLDER_TEXT));

            return true;
        }

        return false;
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
