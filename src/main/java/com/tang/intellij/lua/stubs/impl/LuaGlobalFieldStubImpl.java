package com.tang.intellij.lua.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.comment.psi.LuaDocGlobalDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.stubs.LuaGlobalFieldStub;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public class LuaGlobalFieldStubImpl extends StubBase<LuaDocGlobalDef> implements LuaGlobalFieldStub {

    private String[] names;

    public LuaGlobalFieldStubImpl(StubElement parent, String[] names) {
        super(parent, LuaElementType.GLOBAL_FIELD_DEF);
        this.names = names;
    }

    public String[] getNames() {
        return names;
    }
}
