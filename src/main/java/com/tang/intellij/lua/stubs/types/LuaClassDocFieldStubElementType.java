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
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.comment.psi.impl.LuaDocFieldDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.stubs.LuaDocClassFieldStub;
import com.tang.intellij.lua.stubs.impl.LuaClassFieldStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaClassDocFieldStubElementType extends IStubElementType<LuaDocClassFieldStub, LuaDocFieldDef> {
    public LuaClassDocFieldStubElementType() {
        super("Class Doc Field", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaDocFieldDef createPsi(@NotNull LuaDocClassFieldStub luaFieldStub) {
        return new LuaDocFieldDefImpl(luaFieldStub, this);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        LuaDocFieldDef element = (LuaDocFieldDef) node.getPsi();
        LuaComment comment = LuaCommentUtil.findContainer(element);
        return comment.getClassDef() != null && element.getNameIdentifier() != null;
    }

    @NotNull
    @Override
    public LuaDocClassFieldStub createStub(@NotNull LuaDocFieldDef luaDocFieldDef, StubElement stubElement) {
        LuaComment comment = LuaCommentUtil.findContainer(luaDocFieldDef);
        String name = luaDocFieldDef.getName();
        assert name != null;
        LuaDocClassDef classDef = comment.getClassDef();
        String className = null;
        if (classDef != null) {
            className = classDef.getName();
        }

        return new LuaClassFieldStubImpl(stubElement, name, StringRef.fromNullableString(className));
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class.field";
    }

    @Override
    public void serialize(@NotNull LuaDocClassFieldStub luaFieldStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeUTFFast(luaFieldStub.getName());
        stubOutputStream.writeName(luaFieldStub.getClassName().toString());
    }

    @NotNull
    @Override
    public LuaDocClassFieldStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        String name = stubInputStream.readUTFFast();
        StringRef className = stubInputStream.readName();
        return new LuaClassFieldStubImpl(stubElement, name, className);
    }

    @Override
    public void indexStub(@NotNull LuaDocClassFieldStub luaFieldStub, @NotNull IndexSink indexSink) {
        StringRef className = luaFieldStub.getClassName();
        indexSink.occurrence(LuaClassFieldIndex.KEY, className.getString());
        indexSink.occurrence(LuaClassFieldIndex.KEY, className + "." + luaFieldStub.getName());
    }
}
