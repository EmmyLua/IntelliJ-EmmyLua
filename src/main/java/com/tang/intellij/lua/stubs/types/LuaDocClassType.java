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
import com.intellij.util.io.StringRef;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.comment.psi.LuaDocClassNameRef;
import com.tang.intellij.lua.comment.psi.impl.LuaDocClassDefImpl;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaAssignStat;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaVar;
import com.tang.intellij.lua.psi.LuaVarList;
import com.tang.intellij.lua.stubs.LuaDocClassStub;
import com.tang.intellij.lua.stubs.impl.LuaDocClassStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex;
import com.tang.intellij.lua.stubs.index.LuaSuperClassIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/11/28.
 */
public class LuaDocClassType extends IStubElementType<LuaDocClassStub, LuaDocClassDef> {
    public LuaDocClassType() {
        super("Class", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaDocClassDef createPsi(@NotNull LuaDocClassStub luaDocClassStub) {
        return new LuaDocClassDefImpl(luaDocClassStub, this);
    }

    @NotNull
    @Override
    public LuaDocClassStub createStub(@NotNull LuaDocClassDef luaDocClassDef, StubElement stubElement) {
        LuaDocClassNameRef superClassNameRef = luaDocClassDef.getSuperClassNameRef();
        String superClassName = superClassNameRef == null ? null : superClassNameRef.getText();
        String aliasName = null;
        LuaCommentOwner owner = LuaCommentUtil.findOwner(luaDocClassDef);
        if (owner instanceof LuaAssignStat) {
            LuaAssignStat assignStat = (LuaAssignStat) owner;
            LuaVarList varList = assignStat.getVarList();
            LuaVar var = varList.getVarList().get(0);
            aliasName = var.getText();
        }

        return new LuaDocClassStubImpl(luaDocClassDef.getName(), aliasName, superClassName, stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class";
    }

    @Override
    public void serialize(@NotNull LuaDocClassStub luaDocClassStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeName(luaDocClassStub.getClassName());
        stubOutputStream.writeName(luaDocClassStub.getAliasName());
        stubOutputStream.writeName(luaDocClassStub.getSuperClassName());
    }

    @NotNull
    @Override
    public LuaDocClassStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        StringRef className = stubInputStream.readName();
        StringRef aliasName = stubInputStream.readName();
        StringRef superClassName = stubInputStream.readName();
        return new LuaDocClassStubImpl(StringRef.toString(className), StringRef.toString(aliasName), StringRef.toString(superClassName), stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaDocClassStub luaDocClassStub, @NotNull IndexSink indexSink) {
        LuaType classType = luaDocClassStub.getClassType();
        indexSink.occurrence(LuaClassIndex.KEY, classType.getClassName());
        indexSink.occurrence(LuaShortNameIndex.KEY, classType.getClassName());

        String superClassName = classType.getSuperClassName();
        if (superClassName != null) {
            indexSink.occurrence(LuaSuperClassIndex.KEY, superClassName);
        }
    }
}
