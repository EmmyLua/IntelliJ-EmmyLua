package com.tang.intellij.lua.editor.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/3.
 */
public class LuaScriptBlock extends AbstractBlock {

    //格式化时
    private TokenSet fakeBlockSet = TokenSet.create(
            LuaTypes.BLOCK,
            LuaTypes.FIELD_LIST
    );

    //回车时
    private TokenSet childAttrSet = TokenSet.orSet(fakeBlockSet, TokenSet.create(
            LuaTypes.IF_STAT,
            LuaTypes.DO_STAT,
            LuaTypes.FUNC_BODY,
            LuaTypes.FOR_A_STAT,
            LuaTypes.FOR_B_STAT,
            LuaTypes.REPEAT_STAT,
            LuaTypes.TABLE_CONSTRUCTOR
    ));

    private SpacingBuilder spacingBuilder;
    private Indent indent;

    LuaScriptBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, Indent indent, SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
        this.indent = indent;
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
            if (fakeBlockSet.contains(node.getElementType())) {
                buildChildren(node, results);
            } else if (shouldCreateBlockFor(node)) {
                results.add(new LuaScriptBlock(node, null, null, childIndent, spacingBuilder));
            }
            node = node.getTreeNext();
        }
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
