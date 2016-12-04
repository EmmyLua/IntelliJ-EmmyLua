package com.tang.intellij.lua.psi;

import com.tang.intellij.lua.doc.psi.LuaDocClassDef;

/**
 *
 * Created by tangzx on 2016/12/1.
 */
public interface LuaTypeResolvable extends LuaPsiElement {
    LuaDocClassDef resolveType();
}
