package com.tang.intellij.lua.psi.stub.elements;

import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaClassMethodFuncDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.impl.LuaClassMethodFuncDefImpl;
import com.tang.intellij.lua.psi.stub.LuaClassMethodStub;
import com.tang.intellij.lua.psi.stub.impl.LuaClassMethodStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodStubElementType extends IStubElementType<LuaClassMethodStub, LuaClassMethodFuncDef> {
    public LuaClassMethodStubElementType() {
        super("LuaClassMethodStubElementType", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaClassMethodFuncDef createPsi(@NotNull LuaClassMethodStub luaClassMethodStub) {
        return new LuaClassMethodFuncDefImpl(luaClassMethodStub, LuaElementType.CLASS_METHOD_DEF);
    }

    @Override
    public LuaClassMethodStub createStub(@NotNull LuaClassMethodFuncDef luaClassMethodFuncDef, StubElement stubElement) {
        return new LuaClassMethodStubImpl(stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class_method";
    }

    @Override
    public void serialize(@NotNull LuaClassMethodStub luaClassMethodStub, @NotNull StubOutputStream stubOutputStream) throws IOException {

    }

    @NotNull
    @Override
    public LuaClassMethodStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        return new LuaClassMethodStubImpl(stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaClassMethodStub luaClassMethodStub, @NotNull IndexSink indexSink) {

    }
}
