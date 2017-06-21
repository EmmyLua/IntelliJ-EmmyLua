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

import com.intellij.psi.stubs.*;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaTableExpr;
import com.tang.intellij.lua.psi.impl.LuaTableExprImpl;
import com.tang.intellij.lua.stubs.LuaTableStub;
import com.tang.intellij.lua.stubs.impl.LuaTableStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * table
 * Created by tangzx on 2017/1/12.
 */
public class LuaTableType extends IStubElementType<LuaTableStub, LuaTableExpr> {
    public LuaTableType() {
        super("Table", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaTableExpr createPsi(@NotNull LuaTableStub luaTableStub) {
        return new LuaTableExprImpl(luaTableStub, this);
    }

    @NotNull
    @Override
    public LuaTableStub createStub(@NotNull LuaTableExpr tableConstructor, StubElement stubElement) {
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
