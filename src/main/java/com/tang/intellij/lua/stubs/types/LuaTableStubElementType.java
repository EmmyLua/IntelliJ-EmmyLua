package com.tang.intellij.lua.stubs.types;

import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaTableConstructor;
import com.tang.intellij.lua.psi.impl.LuaTableConstructorImpl;
import com.tang.intellij.lua.stubs.LuaTableStub;
import com.tang.intellij.lua.stubs.impl.LuaTableStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * table
 * Created by tangzx on 2017/1/12.
 */
public class LuaTableStubElementType extends IStubElementType<LuaTableStub, LuaTableConstructor> {
    public LuaTableStubElementType() {
        super("Table", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaTableConstructor createPsi(@NotNull LuaTableStub luaTableStub) {
        return new LuaTableConstructorImpl(luaTableStub, this);
    }

    @NotNull
    @Override
    public LuaTableStub createStub(@NotNull LuaTableConstructor tableConstructor, StubElement stubElement) {
        return new LuaTableStubImpl(stubElement, this);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.table";
    }

    @Override
    public void serialize(@NotNull LuaTableStub luaTableStub, @NotNull StubOutputStream stubOutputStream) throws IOException {

    }

    @NotNull
    @Override
    public LuaTableStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        return new LuaTableStubImpl(stubElement, this);
    }

    @Override
    public void indexStub(@NotNull LuaTableStub luaTableStub, @NotNull IndexSink indexSink) {

    }
}
