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
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.tang.intellij.lua.psi.LuaTypes.*;

/**
 * binary
 * Created by tangzx on 2017/4/23.
 */
public class LuaBinaryScriptBlock extends LuaScriptBlock {

    //这几特殊一点，前后必须要有空格
    private TokenSet AND_NOT_OR = TokenSet.create(AND, NOT, OR);

    LuaBinaryScriptBlock(LuaScriptBlock parent, @NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, Indent indent, SpacingBuilder spacingBuilder) {
        super(parent, node, wrap, alignment, indent, spacingBuilder);
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        LuaScriptBlock c1 = (LuaScriptBlock)child1;
        LuaScriptBlock c2 = (LuaScriptBlock)child2;
        assert c1 != null;
        if (c1.getNode().findChildByType(AND_NOT_OR) != null || c2.getNode().findChildByType(AND_NOT_OR) != null)
            return Spacing.createSpacing(1,1,0,true,1);
        return super.getSpacing(child1, child2);
    }
}
