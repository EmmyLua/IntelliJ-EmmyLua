package com.tang.intellij.lua.psi.stub.elements;

import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.doc.psi.impl.LuaDocClassDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.index.LuaClassIndex;
import com.tang.intellij.lua.psi.stub.LuaClassDefStub;
import com.tang.intellij.lua.psi.stub.impl.LuaClassDefStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/11/28.
 */
public class LuaClassStubElementType extends IStubElementType<LuaClassDefStub, LuaDocClassDef> {
    public LuaClassStubElementType() {
        super("Class Stub", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaDocClassDef createPsi(@NotNull LuaClassDefStub luaClassDefStub) {
        return new LuaDocClassDefImpl(luaClassDefStub, LuaElementType.CLASS_DEF);
    }

    @Override
    public LuaClassDefStub createStub(@NotNull LuaDocClassDef luaDocClassDef, StubElement stubElement) {
        return new LuaClassDefStubImpl(luaDocClassDef.getClassName().getText(), stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class";
    }

    @Override
    public void serialize(@NotNull LuaClassDefStub luaClassDefStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeUTFFast(luaClassDefStub.getClassName());
    }

    @NotNull
    @Override
    public LuaClassDefStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        return new LuaClassDefStubImpl(stubInputStream.readUTFFast(), stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaClassDefStub luaClassDefStub, @NotNull IndexSink indexSink) {
        indexSink.occurrence(LuaClassIndex.KEY, luaClassDefStub.getClassName());
    }
}
