package com.tang.intellij.lua.lang;

import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.impl.LuaGlobalFuncDefImpl;
import com.tang.intellij.lua.psi.stub.LuaGlobalFuncStub;
import com.tang.intellij.lua.psi.stub.impl.LuaGlobalFuncStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/11/26.
 */
public class LuaGlobalFuncDefStubElementType extends IStubElementType<LuaGlobalFuncStub, LuaGlobalFuncDef> {
    public LuaGlobalFuncDefStubElementType() {
        super("GLOBAL_FUNC_DEF", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaGlobalFuncDef createPsi(@NotNull LuaGlobalFuncStub luaGlobalFuncStub) {
        return new LuaGlobalFuncDefImpl(luaGlobalFuncStub, LuaElementType.GLOBAL_FUNC_DEF);
    }

    @Override
    public LuaGlobalFuncStub createStub(@NotNull LuaGlobalFuncDef globalFuncDef, StubElement stubElement) {
        return new LuaGlobalFuncStubImpl(stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.global_func_def";
    }

    @Override
    public void serialize(@NotNull LuaGlobalFuncStub luaGlobalFuncStub, @NotNull StubOutputStream stubOutputStream) throws IOException {

    }

    @NotNull
    @Override
    public LuaGlobalFuncStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        return new LuaGlobalFuncStubImpl(stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaGlobalFuncStub luaGlobalFuncStub, @NotNull IndexSink indexSink) {

    }
}
