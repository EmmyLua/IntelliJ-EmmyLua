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

package com.tang.intellij.lua.editor.formatter.blocks;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.tang.intellij.lua.psi.LuaTypes.*;

/**
 *
 * Created by tangzx on 2016/12/3.
 */
public class LuaScriptBlock extends AbstractBlock {

    //不创建 ASTBlock
    private TokenSet fakeBlockSet = TokenSet.create(
            BLOCK,
            FIELD_LIST
    );

    //回车时
    private TokenSet childAttrSet = TokenSet.orSet(fakeBlockSet, TokenSet.create(
            IF_STAT,
            DO_STAT,
            FUNC_BODY,
            FOR_A_STAT,
            FOR_B_STAT,
            REPEAT_STAT,
            WHILE_STAT,
            TABLE_CONSTRUCTOR
    ));

    private IElementType elementType;
    private SpacingBuilder spacingBuilder;
    private Indent indent;
    private LuaScriptBlock parent;

    private Alignment alignment;

    public LuaScriptBlock(LuaScriptBlock parent,
                   @NotNull ASTNode node,
                   @Nullable Wrap wrap,
                   @Nullable Alignment alignment,
                   Indent indent,
                   SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.alignment = alignment;
        this.spacingBuilder = spacingBuilder;
        this.indent = indent;
        this.parent = parent;
        this.elementType = node.getElementType();
    }

    private static boolean shouldCreateBlockFor(ASTNode node) {
        return node.getTextRange().getLength() != 0 && node.getElementType() != TokenType.WHITE_SPACE;
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<>();
        buildChildren(myNode, blocks);
        return blocks;
    }

    private void buildChildren(ASTNode parent, List<Block> results) {
        ASTNode node = parent.getFirstChildNode();
        IElementType parentType = parent.getElementType();
        Indent childIndent = Indent.getNoneIndent();
        if (fakeBlockSet.contains(parentType)) {
            childIndent = Indent.getNormalIndent();
        }

        while (node != null) {
            IElementType nodeElementType = node.getElementType();
            if (fakeBlockSet.contains(nodeElementType)) {
                buildChildren(node, results);
            } else if (shouldCreateBlockFor(node)) {
                results.add(createBlock(node, childIndent, null));
            }
            node = node.getTreeNext();
        }
    }

    @NotNull
    private LuaScriptBlock createBlock(ASTNode node, Indent childIndent, Alignment alignment) {
        if (node.getElementType() == UNARY_EXPR)
            return new LuaUnaryScriptBlock(this, node, null, alignment, childIndent, spacingBuilder);
        if (node.getElementType() == BINARY_EXPR)
            return new LuaBinaryScriptBlock(this, node, null, alignment, childIndent, spacingBuilder);
        return new LuaScriptBlock(this, node, null, alignment, childIndent, spacingBuilder);
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Nullable
    @Override
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    public Indent getIndent() {
        return indent;
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        if (childAttrSet.contains(elementType))
            return new ChildAttributes(Indent.getNormalIndent(), null);
        return new ChildAttributes(Indent.getNoneIndent(), null);
    }
}
