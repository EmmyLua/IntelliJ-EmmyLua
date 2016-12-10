package com.tang.intellij.lua.psi.stub.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.tang.intellij.lua.doc.psi.LuaDocFieldDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.stub.LuaClassFieldStub;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaClassFieldStubImpl extends StubBase<LuaDocFieldDef> implements LuaClassFieldStub {
    private String name;
    private StringRef className;

    public LuaClassFieldStubImpl(StubElement parent, String name, StringRef className) {
        super(parent, LuaElementType.CLASS_FIELD_DEF);
        this.name = name;
        this.className = className;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StringRef getClassName() {
        return className;
    }
}
