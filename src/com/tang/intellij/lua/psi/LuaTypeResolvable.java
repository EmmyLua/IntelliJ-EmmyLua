package com.tang.intellij.lua.psi;

import com.tang.intellij.lua.lang.type.LuaTypeSet;

/**
 *
 * Created by tangzx on 2016/12/1.
 */
public interface LuaTypeResolvable extends LuaPsiElement {
    LuaTypeSet resolveType();
}
