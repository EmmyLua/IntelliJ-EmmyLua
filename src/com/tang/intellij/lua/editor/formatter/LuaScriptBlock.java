package com.tang.intellij.lua.editor.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
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

    private SpacingBuilder spacingBuilder;

    protected LuaScriptBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<>();
        ASTNode node = myNode.getFirstChildNode();
        while (node != null) {
            IElementType type = node.getElementType();
            if (type == LuaTypes.LOCAL_FUNC_DEF) {
                blocks.add(new LuaScriptBlock(node, myWrap, myAlignment, spacingBuilder));
            }

            node = node.getTreeNext();
        }
        return blocks;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block block, @NotNull Block block1) {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
