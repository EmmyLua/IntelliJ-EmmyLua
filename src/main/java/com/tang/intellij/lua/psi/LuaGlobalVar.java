package com.tang.intellij.lua.psi;

import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/1/16.
 */
public interface LuaGlobalVar extends LuaPsiElement {
    @Nullable
    LuaNameRef getNameRef();
}
