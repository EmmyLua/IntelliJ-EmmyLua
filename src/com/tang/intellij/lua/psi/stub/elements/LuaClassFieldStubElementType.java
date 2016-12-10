package com.tang.intellij.lua.psi.stub.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import com.tang.intellij.lua.doc.LuaCommentUtil;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.doc.psi.LuaDocClassName;
import com.tang.intellij.lua.doc.psi.LuaDocFieldDef;
import com.tang.intellij.lua.doc.psi.LuaDocFieldNameDef;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.doc.psi.impl.LuaDocFieldDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.psi.index.LuaClassFieldIndex;
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
        LuaDocFieldDef element = (LuaDocFieldDef) node.getPsi();
        return element.getFieldNameDef() != null;
    }

    @Override
    public LuaClassFieldStub createStub(@NotNull LuaDocFieldDef luaDocFieldDef, StubElement stubElement) {
        LuaComment comment = LuaCommentUtil.findContainer(luaDocFieldDef);
        LuaDocFieldNameDef nameDef = luaDocFieldDef.getFieldNameDef();
        assert nameDef != null;
        LuaDocClassDef classDef = comment.getClassDef();
        String className = null;
        if (classDef != null) {
            LuaDocClassName docClassName = classDef.getClassName();
            className = docClassName.getName();
        }

        return new LuaClassFieldStubImpl(stubElement, nameDef.getName(), StringRef.fromNullableString(className));
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class.field";
    }

    @Override
    public void serialize(@NotNull LuaClassFieldStub luaFieldStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeUTFFast(luaFieldStub.getName());
        stubOutputStream.writeName(luaFieldStub.getClassName().toString());
    }

    @NotNull
    @Override
    public LuaClassFieldStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        String name = stubInputStream.readUTFFast();
        StringRef className = stubInputStream.readName();
        return new LuaClassFieldStubImpl(stubElement, name, className);
    }

    @Override
    public void indexStub(@NotNull LuaClassFieldStub luaFieldStub, @NotNull IndexSink indexSink) {
        StringRef className = luaFieldStub.getClassName();
        indexSink.occurrence(LuaClassFieldIndex.KEY, className.getString());
    }
}
