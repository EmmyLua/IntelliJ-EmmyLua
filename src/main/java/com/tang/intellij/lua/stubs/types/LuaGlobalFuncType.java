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
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.LuaParamInfo;
import com.tang.intellij.lua.psi.LuaPsiImplUtil;
import com.tang.intellij.lua.psi.impl.LuaGlobalFuncDefImpl;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaGlobalFuncStub;
import com.tang.intellij.lua.stubs.impl.LuaGlobalFuncStubImpl;
import com.tang.intellij.lua.stubs.index.LuaGlobalFuncIndex;
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/11/26.
 */
public class LuaGlobalFuncType extends IStubElementType<LuaGlobalFuncStub, LuaGlobalFuncDef> {

    public LuaGlobalFuncType() {
        super("Global Function", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaGlobalFuncDef createPsi(@NotNull LuaGlobalFuncStub luaGlobalFuncStub) {
        return new LuaGlobalFuncDefImpl(luaGlobalFuncStub, this);
    }

    @NotNull
    @Override
    public LuaGlobalFuncStub createStub(@NotNull LuaGlobalFuncDef globalFuncDef, StubElement stubElement) {
        PsiElement nameRef = globalFuncDef.getNameIdentifier();
        assert nameRef != null;
        SearchContext searchContext = new SearchContext(globalFuncDef.getProject()).setCurrentStubFile(globalFuncDef.getContainingFile());
        LuaTypeSet returnTypeSet = LuaPsiImplUtil.guessReturnTypeSetOriginal(globalFuncDef, searchContext);
        LuaParamInfo[] params = LuaPsiImplUtil.getParamsOriginal(globalFuncDef);

        return new LuaGlobalFuncStubImpl(nameRef.getText(), params, returnTypeSet, stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.global_func_def";
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        PsiElement element = node.getPsi();
        if (element instanceof LuaGlobalFuncDef) {
            LuaGlobalFuncDef globalFuncDef = (LuaGlobalFuncDef) element;
            return globalFuncDef.getNameIdentifier() != null;
        }
        return false;
    }

    @Override
    public void serialize(@NotNull LuaGlobalFuncStub luaGlobalFuncStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeName(luaGlobalFuncStub.getName());

        // params
        LuaParamInfo[] params = luaGlobalFuncStub.getParams();
        stubOutputStream.writeByte(params.length);
        for (LuaParamInfo param : params) {
            LuaParamInfo.serialize(param, stubOutputStream);
        }

        LuaTypeSet.serialize(luaGlobalFuncStub.getReturnType(), stubOutputStream);
    }

    @NotNull
    @Override
    public LuaGlobalFuncStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        StringRef name = stubInputStream.readName();

        // params
        int len = stubInputStream.readByte();
        LuaParamInfo[] params = new LuaParamInfo[len];
        for (int i = 0; i < len; i++) {
            params[i] = LuaParamInfo.deserialize(stubInputStream);
        }

        LuaTypeSet returnTypeSet = LuaTypeSet.deserialize(stubInputStream);
        return new LuaGlobalFuncStubImpl(StringRef.toString(name), params, returnTypeSet, stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaGlobalFuncStub luaGlobalFuncStub, @NotNull IndexSink indexSink) {
        String name = luaGlobalFuncStub.getName();
        indexSink.occurrence(LuaShortNameIndex.KEY, name);
        indexSink.occurrence(LuaGlobalFuncIndex.KEY, name);
    }
}
