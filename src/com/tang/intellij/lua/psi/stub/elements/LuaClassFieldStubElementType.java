package com.tang.intellij.lua.psi.stub.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.*;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.doc.psi.LuaDocFieldDef;
import com.tang.intellij.lua.doc.psi.impl.LuaDocFieldDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.stub.LuaClassFieldStub;
import com.tang.intellij.lua.psi.stub.impl.LuaClassFieldStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaClassFieldStubElementType extends IStubElementType<LuaClassFieldStub, LuaDocFieldDef> {
    public LuaClassFieldStubElementType() {
        super("Lua Field", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaDocFieldDef createPsi(@NotNull LuaClassFieldStub luaFieldStub) {
        return new LuaDocFieldDefImpl(luaFieldStub, LuaElementType.CLASS_FIELD_DEF);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        IElementType elementType = node.getElementType();
        return super.shouldCreateStub(node);
    }

    @Override
    public LuaClassFieldStub createStub(@NotNull LuaDocFieldDef luaDocFieldDef, StubElement stubElement) {
        return new LuaClassFieldStubImpl(stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class.field";
    }

    @Override
    public void serialize(@NotNull LuaClassFieldStub luaFieldStub, @NotNull StubOutputStream stubOutputStream) throws IOException {

    }

    @NotNull
    @Override
    public LuaClassFieldStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        return new LuaClassFieldStubImpl(stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaClassFieldStub luaFieldStub, @NotNull IndexSink indexSink) {

    }
}
