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

package com.tang.intellij.lua.editor.formatter;

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

    private SpacingBuilder spacingBuilder;
    private Indent indent;
    private LuaScriptBlock parent;

    private Alignment callAlignment;
    private Alignment assignAlignment;

    LuaScriptBlock(LuaScriptBlock parent,
                   @NotNull ASTNode node,
                   @Nullable Wrap wrap,
                   @Nullable Alignment alignment,
                   Indent indent,
                   SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
        this.indent = indent;
        this.parent = parent;

        assignAlignment = Alignment.createAlignment(true);

        IElementType elementType = node.getElementType();
        if (elementType == CALL_EXPR || elementType == INDEX_EXPR)
            callAlignment = Alignment.createAlignment(true);
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
                Alignment alignment = null;
                if (parentType == CALL_EXPR || parentType == INDEX_EXPR) {
                    if (nodeElementType == COLON || nodeElementType == DOT) {
                        alignment = getTopmostCallAlignment();
                    }
                } else if (parentType == LOCAL_DEF || parentType == ASSIGN_STAT) {
                    if (nodeElementType == ASSIGN) {
                        alignment = this.parent.assignAlignment;
                    }
                } else if (parentType == TABLE_FIELD) {
                    if (nodeElementType == ASSIGN) {
                        alignment = this.parent.parent.assignAlignment;
                    }
                }
                results.add(new LuaScriptBlock(this, node, null, alignment, childIndent, spacingBuilder));
            }
            node = node.getTreeNext();
        }
    }

    @Nullable
    private Alignment getTopmostCallAlignment() {
        Alignment alignment = null;
        LuaScriptBlock callNode = this;
        while (callNode != null && callNode.callAlignment != null) {
            alignment = callNode.callAlignment;
            callNode = callNode.parent;
        }
        return alignment;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block block, @NotNull Block block1) {
        return spacingBuilder.getSpacing(this, block, block1);
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
        if (childAttrSet.contains(myNode.getElementType()))
            return new ChildAttributes(Indent.getNormalIndent(), null);
        return new ChildAttributes(Indent.getNoneIndent(), null);
    }
}
