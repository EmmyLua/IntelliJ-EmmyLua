package com.tang.intellij.lua.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.psi.LuaTableConstructor;
import com.tang.intellij.lua.stubs.LuaTableStub;

/**
 *
 * Created by tangzx on 2017/1/12.
 */
public class LuaTableStubImpl extends StubBase<LuaTableConstructor> implements LuaTableStub {
    public LuaTableStubImpl(StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
