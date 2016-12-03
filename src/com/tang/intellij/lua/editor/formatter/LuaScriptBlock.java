package com.tang.intellij.lua.editor.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
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

    TokenSet set = TokenSet.create(
            LuaTypes.IF_STAT,
            LuaTypes.DO_STAT,
            LuaTypes.FUNC_BODY,
            LuaTypes.BLOCK
    );

    private SpacingBuilder spacingBuilder;
    private Indent indent;

    protected LuaScriptBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, Indent indent, SpacingBuilder spacingBuilder) {
        super(node, null, null);
        this.spacingBuilder = spacingBuilder;
        this.indent = indent;
    }

    private static boolean shouldCreateBlockFor(ASTNode node) {
        return node.getTextRange().getLength() != 0 && node.getElementType() != TokenType.WHITE_SPACE;
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<>();
        ASTNode node = myNode.getFirstChildNode();
        while (node != null) {
            if (shouldCreateBlockFor(node)) {
                Indent childIndent = Indent.getNoneIndent();
                if (myNode.getElementType() == LuaTypes.BLOCK) {
                    childIndent = Indent.getNormalIndent();
                }

                blocks.add(new LuaScriptBlock(node, myWrap, myAlignment, childIndent, spacingBuilder));
            }

            node = node.getTreeNext();
        }
        return blocks;
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
        if (set.contains(myNode.getElementType()))
            return new ChildAttributes(Indent.getNormalIndent(), null);
        return super.getChildAttributes(newChildIndex);
    }
}
