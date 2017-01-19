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
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.impl.LuaDocClassDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.stubs.LuaClassDefStub;
import com.tang.intellij.lua.stubs.impl.LuaClassDefStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/11/28.
 */
public class LuaClassStubElementType extends IStubElementType<LuaClassDefStub, LuaDocClassDef> {
    public LuaClassStubElementType() {
        super("Class", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaDocClassDef createPsi(@NotNull LuaClassDefStub luaClassDefStub) {
        return new LuaDocClassDefImpl(luaClassDefStub, LuaElementType.CLASS_DEF);
    }

    @NotNull
    @Override
    public LuaClassDefStub createStub(@NotNull LuaDocClassDef luaDocClassDef, StubElement stubElement) {
        return new LuaClassDefStubImpl(luaDocClassDef.getName(), stubElement);
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
        indexSink.occurrence(LuaShortNameIndex.KEY, luaClassDefStub.getClassName());
    }
}
