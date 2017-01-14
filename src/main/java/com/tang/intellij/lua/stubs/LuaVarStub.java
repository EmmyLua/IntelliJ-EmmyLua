package com.tang.intellij.lua.stubs;

import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.psi.LuaVar;

/**
 * xxx.xx = value
 * Created by tangzx on 2017/1/12.
 */
public interface LuaVarStub extends StubElement<LuaVar> {
    String getTypeName();
    String getFieldName();
}
