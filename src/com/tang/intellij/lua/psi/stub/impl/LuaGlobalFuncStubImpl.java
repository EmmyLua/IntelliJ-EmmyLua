package com.tang.intellij.lua.psi.stub.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.LuaTypes;
import com.tang.intellij.lua.psi.stub.LuaGlobalFuncStub;

/**
 *
 * Created by tangzx on 2016/11/26.
 */
public class LuaGlobalFuncStubImpl extends StubBase<LuaGlobalFuncDef> implements LuaGlobalFuncStub {
    public LuaGlobalFuncStubImpl(StubElement parent) {
        super(parent, (IStubElementType) LuaTypes.GLOBAL_FUNC_DEF);
    }
}