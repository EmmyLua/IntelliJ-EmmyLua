package com.tang.intellij.lua.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaTableField;
import com.tang.intellij.lua.psi.impl.LuaTableFieldImpl;
import com.tang.intellij.lua.stubs.LuaTableFieldStub;
import com.tang.intellij.lua.stubs.impl.LuaTableFieldStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2017/1/14.
 */
public class LuaTableFieldStubElementType extends IStubElementType<LuaTableFieldStub, LuaTableField> {
    public LuaTableFieldStubElementType() {
        super("Table Field", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaTableField createPsi(@NotNull LuaTableFieldStub luaTableFieldStub) {
        return new LuaTableFieldImpl(luaTableFieldStub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        LuaTableField tableField = (LuaTableField) node.getPsi();
        return tableField.getId() != null;
    }

    @NotNull
    @Override
    public LuaTableFieldStub createStub(@NotNull LuaTableField field, StubElement stubElement) {
        return new LuaTableFieldStubImpl(field, stubElement, this);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.table_field";
    }

    @Override
    public void serialize(@NotNull LuaTableFieldStub fieldStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        String typeName = fieldStub.getTypeName();
        stubOutputStream.writeUTFFast(typeName);
        String fieldName = fieldStub.getFieldName();
        stubOutputStream.writeUTFFast(fieldName);
    }

    @NotNull
    @Override
    public LuaTableFieldStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        String typeName = stubInputStream.readUTFFast();
        String fieldName = stubInputStream.readUTFFast();
        return new LuaTableFieldStubImpl(typeName, fieldName, stubElement, this);
    }

    @Override
    public void indexStub(@NotNull LuaTableFieldStub fieldStub, @NotNull IndexSink indexSink) {
        String fieldName = fieldStub.getFieldName();
        String typeName = fieldStub.getTypeName();
        if (fieldName != null && typeName != null) {
            indexSink.occurrence(LuaClassFieldIndex.KEY, fieldName);
            indexSink.occurrence(LuaClassFieldIndex.KEY, typeName + "." + fieldName);
        }
    }
}
