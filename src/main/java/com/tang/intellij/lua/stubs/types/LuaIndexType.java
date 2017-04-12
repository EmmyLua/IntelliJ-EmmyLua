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
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaVar;
import com.tang.intellij.lua.psi.LuaVarList;
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl;
import com.tang.intellij.lua.stubs.LuaIndexStub;
import com.tang.intellij.lua.stubs.impl.LuaIndexStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by TangZX on 2017/4/12.
 */
public class LuaIndexType extends IStubElementType<LuaIndexStub, LuaIndexExpr> {
    public LuaIndexType() {
        super("LuaIndex", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaIndexExpr createPsi(@NotNull LuaIndexStub indexStub) {
        return new LuaIndexExprImpl(indexStub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        LuaIndexExpr psi = (LuaIndexExpr) node.getPsi();
        if (psi.getId() != null) {
            if (psi.getParent() instanceof LuaVar) {
                if (psi.getParent().getParent() instanceof LuaVarList) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public LuaIndexStub createStub(@NotNull LuaIndexExpr indexExpr, StubElement stubElement) {
        return new LuaIndexStubImpl(indexExpr, stubElement, this);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.index_expr";
    }

    @Override
    public void serialize(@NotNull LuaIndexStub indexStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeName(indexStub.getTypeName());
        stubOutputStream.writeName(indexStub.getFieldName());
    }

    @NotNull
    @Override
    public LuaIndexStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        StringRef typeName = stubInputStream.readName();
        StringRef fieldName = stubInputStream.readName();

        return new LuaIndexStubImpl(StringRef.toString(typeName), StringRef.toString(fieldName), stubElement, this);
    }

    @Override
    public void indexStub(@NotNull LuaIndexStub indexStub, @NotNull IndexSink indexSink) {
        String fieldName = indexStub.getFieldName();
        String typeName = indexStub.getTypeName();
        if (typeName != null && fieldName != null) {
            indexSink.occurrence(LuaClassFieldIndex.KEY, typeName);
            indexSink.occurrence(LuaClassFieldIndex.KEY, typeName + "." + fieldName);
            indexSink.occurrence(LuaShortNameIndex.KEY, fieldName);
        }
    }
}
