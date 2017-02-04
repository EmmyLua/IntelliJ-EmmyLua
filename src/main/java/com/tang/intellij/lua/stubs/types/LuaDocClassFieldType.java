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
import com.tang.intellij.lua.comment.psi.LuaDocPsiImplUtil;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.comment.psi.impl.LuaDocFieldDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaDocClassFieldStub;
import com.tang.intellij.lua.stubs.impl.LuaDocClassFieldStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaDocClassFieldType extends IStubElementType<LuaDocClassFieldStub, LuaDocFieldDef> {
    public LuaDocClassFieldType() {
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

        SearchContext searchContext = new SearchContext(luaDocFieldDef.getProject()).setCurrentStubFile(luaDocFieldDef.getContainingFile());
        LuaTypeSet typeSet = LuaDocPsiImplUtil.resolveDocTypeSet(luaDocFieldDef.getTypeSet(), null, searchContext);

        return new LuaDocClassFieldStubImpl(stubElement, name, className, typeSet);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class.field";
    }

    @Override
    public void serialize(@NotNull LuaDocClassFieldStub luaFieldStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeName(luaFieldStub.getName());
        stubOutputStream.writeName(luaFieldStub.getClassName());
        LuaTypeSet.serialize(luaFieldStub.getType(), stubOutputStream);
    }

    @NotNull
    @Override
    public LuaDocClassFieldStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        StringRef name = stubInputStream.readName();
        StringRef className = stubInputStream.readName();
        LuaTypeSet typeSet = LuaTypeSet.deserialize(stubInputStream);
        return new LuaDocClassFieldStubImpl(stubElement, StringRef.toString(name), StringRef.toString(className), typeSet);
    }

    @Override
    public void indexStub(@NotNull LuaDocClassFieldStub luaFieldStub, @NotNull IndexSink indexSink) {
        String className = luaFieldStub.getClassName();
        indexSink.occurrence(LuaClassFieldIndex.KEY, className);
        indexSink.occurrence(LuaClassFieldIndex.KEY, className + "." + luaFieldStub.getName());
    }
}
