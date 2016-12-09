package com.tang.intellij.lua.psi.stub.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.doc.psi.LuaDocGlobalDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.stub.LuaGlobalFieldStub;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public class LuaGlobalFieldStubImpl extends StubBase<LuaDocGlobalDef> implements LuaGlobalFieldStub {

    public LuaGlobalFieldStubImpl(StubElement parent) {
        super(parent, LuaElementType.GLOBAL_FUNC_DEF);
    }
}
