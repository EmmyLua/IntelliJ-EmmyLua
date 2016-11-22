package com.tang.intellij.lua.psi.stub.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.stub.LuaGlobalFuncDefStub;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class LuaGlobalFuncDefStubImpl extends StubBase<LuaGlobalFuncDef> implements LuaGlobalFuncDefStub {
    public LuaGlobalFuncDefStubImpl(StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
