/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
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
public class LuaTableFieldType extends IStubElementType<LuaTableFieldStub, LuaTableField> {
    public LuaTableFieldType() {
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
        stubOutputStream.writeName(typeName);
        String fieldName = fieldStub.getFieldName();
        stubOutputStream.writeName(fieldName);
    }

    @NotNull
    @Override
    public LuaTableFieldStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        StringRef typeName = stubInputStream.readName();
        StringRef fieldName = stubInputStream.readName();
        return new LuaTableFieldStubImpl(StringRef.toString(typeName), StringRef.toString(fieldName), stubElement, this);
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
