package com.tang.intellij.lua.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaVar;
import com.tang.intellij.lua.stubs.LuaVarStub;

/**
 *
 * Created by tangzx on 2017/1/12.
 */
public class LuaClassVarFieldStubImpl extends StubBase<LuaVar> implements LuaVarStub {

    public String getTypeName() {
        return typeName;
    }

    private String typeName;

    public LuaClassVarFieldStubImpl(StubElement parent, IStubElementType elementType, String typeName) {
        super(parent, elementType);
        this.typeName = typeName;
    }
}
