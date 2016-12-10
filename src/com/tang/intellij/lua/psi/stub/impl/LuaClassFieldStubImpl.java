package com.tang.intellij.lua.psi.stub.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.doc.psi.LuaDocFieldDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.stub.LuaClassFieldStub;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaClassFieldStubImpl extends StubBase<LuaDocFieldDef> implements LuaClassFieldStub {
    public LuaClassFieldStubImpl(StubElement parent) {
        super(parent, LuaElementType.CLASS_FIELD_DEF);
    }
}
