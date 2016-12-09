package com.tang.intellij.lua.psi.stub.elements;

import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.doc.psi.LuaDocGlobalDef;
import com.tang.intellij.lua.doc.psi.impl.LuaDocGlobalDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.stub.LuaGlobalFieldStub;
import com.tang.intellij.lua.psi.stub.impl.LuaGlobalFieldStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public class LuaGlobalFieldStubElementType extends IStubElementType<LuaGlobalFieldStub, LuaDocGlobalDef> {

    public LuaGlobalFieldStubElementType() {
        super("Global Field", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaDocGlobalDef createPsi(@NotNull LuaGlobalFieldStub luaGlobalFieldStub) {
        return new LuaDocGlobalDefImpl(luaGlobalFieldStub, LuaElementType.GLOBAL_FUNC_DEF);
    }

    @Override
    public LuaGlobalFieldStub createStub(@NotNull LuaDocGlobalDef luaDocGlobalDef, StubElement stubElement) {
        return new LuaGlobalFieldStubImpl(stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.global.field";
    }

    @Override
    public void serialize(@NotNull LuaGlobalFieldStub luaGlobalFieldStub, @NotNull StubOutputStream stubOutputStream) throws IOException {

    }

    @NotNull
    @Override
    public LuaGlobalFieldStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        return new LuaGlobalFieldStubImpl(stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaGlobalFieldStub luaGlobalFieldStub, @NotNull IndexSink indexSink) {

    }
}
