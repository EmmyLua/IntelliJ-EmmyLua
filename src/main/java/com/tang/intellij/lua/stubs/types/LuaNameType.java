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
import com.tang.intellij.lua.psi.LuaNameExpr;
import com.tang.intellij.lua.psi.LuaVarList;
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl;
import com.tang.intellij.lua.stubs.LuaNameStub;
import com.tang.intellij.lua.stubs.impl.LuaNameStubImpl;
import com.tang.intellij.lua.stubs.index.LuaGlobalVarIndex;
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * global var
 * Created by TangZX on 2017/4/12.
 */
public class LuaNameType extends IStubElementType<LuaNameStub, LuaNameExpr> {
    public LuaNameType() {
        super("NameExpr", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaNameExpr createPsi(@NotNull LuaNameStub luaNameStub) {
        return new LuaNameExprImpl(luaNameStub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        LuaNameExpr psi = (LuaNameExpr) node.getPsi();
        return psi.getParent() instanceof LuaVarList;
    }

    @NotNull
    @Override
    public LuaNameStub createStub(@NotNull LuaNameExpr luaNameExpr, StubElement stubElement) {
        return new LuaNameStubImpl(luaNameExpr, stubElement, this);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.name_expr";
    }

    @Override
    public void serialize(@NotNull LuaNameStub luaNameStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeName(luaNameStub.getName());
    }

    @NotNull
    @Override
    public LuaNameStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        StringRef nameRef = stubInputStream.readName();
        return new LuaNameStubImpl(StringRef.toString(nameRef), stubElement, this);
    }

    @Override
    public void indexStub(@NotNull LuaNameStub luaNameStub, @NotNull IndexSink indexSink) {
        if (luaNameStub.isGlobal()) {
            indexSink.occurrence(LuaGlobalVarIndex.KEY, luaNameStub.getName());
            indexSink.occurrence(LuaShortNameIndex.KEY, luaNameStub.getName());
        }
    }
}
