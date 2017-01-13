package com.tang.intellij.lua.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaVar;
import com.tang.intellij.lua.stubs.LuaVarStub;

/**
 *
 * Created by tangzx on 2017/1/12.
 */
public class LuaClassVarFieldStubImpl extends StubBase<LuaVar> implements LuaVarStub {

    private LuaIndexExpr indexExpr;
    private String typeName;

    public LuaClassVarFieldStubImpl(StubElement parent,
                                    IStubElementType elementType,
                                    LuaIndexExpr indexExpr) {
        super(parent, elementType);
        this.indexExpr = indexExpr;
    }

    public LuaClassVarFieldStubImpl(StubElement stubElement,
                                    IStubElementType type,
                                    String typeName) {
        super(stubElement, type);
        this.typeName = typeName;
    }

    public String getTypeName() {
        if (typeName == null) {
            LuaTypeSet set = indexExpr.guessPrefixType();
            if (set != null) {
                LuaType type = set.getType(0);
                typeName = type.getClassNameText();
            }
        }
        return typeName;
    }

}
