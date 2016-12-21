package com.tang.intellij.lua.psi;

import org.jetbrains.annotations.Nullable;

/**
 * #local function
 * #function
 * #lambda function
 * #class method
 *
 * Created by TangZX on 2016/12/9.
 */
public interface LuaFuncBodyOwner extends LuaParametersOwner {
    @Nullable
    LuaFuncBody getFuncBody();
}
