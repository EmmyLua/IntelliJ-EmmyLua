package com.tang.intellij.lua.psi.stub;

import com.intellij.psi.stubs.StubElement;
import com.sun.istack.internal.NotNull;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;

/**
 *
 * Created by tangzx on 2016/11/26.
 */
public interface LuaGlobalFuncStub extends StubElement<LuaGlobalFuncDef> {
    @NotNull
    String getName();
}
