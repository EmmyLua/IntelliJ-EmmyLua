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

package com.tang.intellij.lua.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.psi.LuaBlock;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangzx
 * Date : 2015/11/16.
 */
public class LuaFoldingBuilder implements FoldingBuilder {

    static final String HOLDER_TEXT = "...";

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        collectDescriptorsRecursively(node, document, descriptors);
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private  void collectDescriptorsRecursively(@NotNull ASTNode node,
                                                @NotNull Document document,
                                                @NotNull List<FoldingDescriptor> descriptors) {
        IElementType type = node.getElementType();
        if (type == LuaTypes.BLOCK) {
            LuaBlock block = (LuaBlock) node.getPsi();
            PsiElement prev = PsiTreeUtil.skipSiblingsBackward(block, PsiWhiteSpace.class);
            PsiElement next = PsiTreeUtil.skipSiblingsForward(block, PsiWhiteSpace.class);
            if (prev != null && next != null) {
                int l = prev.getTextOffset() + prev.getTextLength();
                int r = next.getTextOffset();

                TextRange range = new TextRange(l, r);
                if (range.getLength() > 0 && !addOneLineMethodFolding(descriptors, block, range, prev, next)) {
                    descriptors.add(new FoldingDescriptor(block, range));
                }
            }
        }
        else if (type == LuaTypes.DOC_COMMENT) {
            descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
        }

        for (ASTNode child : node.getChildren(null)) {
            collectDescriptorsRecursively(child, document, descriptors);
        }
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

            TextRange lRange = new TextRange(range.getStartOffset(), first.getTextOffset());
            TextRange rRange = new TextRange(first.getTextOffset() + first.getTextLength(), range.getEndOffset());
            if (lRange.isEmpty() || rRange.isEmpty())
                return false;

            FoldingGroup group = FoldingGroup.newGroup("one-liner");
            descriptors.add(new NamedFoldingDescriptor(prev.getNode(), lRange, group, HOLDER_TEXT));
            descriptors.add(new NamedFoldingDescriptor(next.getNode(), rRange, group, HOLDER_TEXT));

            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode astNode) {
        IElementType type = astNode.getElementType();
        if (type == LuaTypes.BLOCK) return HOLDER_TEXT;
        else if (type == LuaTypes.DOC_COMMENT) return "/** ... */";
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return false;
    }
}
