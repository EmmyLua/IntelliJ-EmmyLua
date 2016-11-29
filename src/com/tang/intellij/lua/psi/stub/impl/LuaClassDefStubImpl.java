package com.tang.intellij.lua.psi.stub.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.stub.LuaClassDefStub;

/**
 *
 * Created by tangzx on 2016/11/28.
 */
public class LuaClassDefStubImpl extends StubBase<LuaDocClassDef> implements LuaClassDefStub {

    private String className;

    public LuaClassDefStubImpl(String className, StubElement parent) {
        super(parent, LuaElementType.CLASS_DEF);
        this.className = className;
    }

    @Override
    public String getClassName() {
        return className;
    }
}
