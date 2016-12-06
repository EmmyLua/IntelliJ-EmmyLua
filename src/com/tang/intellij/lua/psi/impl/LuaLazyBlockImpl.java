package com.tang.intellij.lua.psi.impl;

import com.intellij.psi.impl.source.tree.LazyParseablePsiElement;
import com.tang.intellij.lua.psi.LuaBlock;
import com.tang.intellij.lua.psi.LuaTypes;

/**
 *
 * Created by TangZX on 2016/12/5.
 */
public class LuaLazyBlockImpl extends LazyParseablePsiElement implements LuaBlock {
    public LuaLazyBlockImpl(CharSequence buffer) {
        super(LuaTypes.BLOCK, buffer);
    }
}
