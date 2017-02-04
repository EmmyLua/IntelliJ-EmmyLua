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
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.psi.impl.LuaClassMethodDefImpl;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaClassMethodStub;
import com.tang.intellij.lua.stubs.impl.LuaClassMethodStubImpl;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodType extends IStubElementType<LuaClassMethodStub, LuaClassMethodDef> {
    public LuaClassMethodType() {
        super("Class Method", LuaLanguage.INSTANCE);
    }

    @Override
    public LuaClassMethodDef createPsi(@NotNull LuaClassMethodStub luaClassMethodStub) {
        return new LuaClassMethodDefImpl(luaClassMethodStub, this);
    }

    @NotNull
    @Override
    public LuaClassMethodStub createStub(@NotNull LuaClassMethodDef methodDef, StubElement stubElement) {
        LuaClassMethodName methodName = methodDef.getClassMethodName();
        PsiElement id = methodDef.getNameIdentifier();
        LuaNameRef nameRef = methodName.getNameRef();
        assert nameRef != null;
        assert id != null;
        String clazzName = nameRef.getText();
        SearchContext searchContext = new SearchContext(methodDef.getProject()).setCurrentStubFile(methodDef.getContainingFile());

        LuaTypeSet typeSet = nameRef.guessType(searchContext);
        if (typeSet != null) {
            LuaType type = typeSet.getFirst();
            if (type != null)
                clazzName = type.getClassNameText();
        }

        LuaTypeSet returnTypeSet = LuaPsiImplUtil.guessReturnTypeSetOriginal(methodDef, searchContext);
        String[] params = LuaPsiImplUtil.getParamsOriginal(methodDef);

        PsiElement prev = id.getPrevSibling();
        boolean isStatic = prev.getNode().getElementType() == LuaTypes.DOT;

        return new LuaClassMethodStubImpl(id.getText(), clazzName, params, returnTypeSet, isStatic, stubElement);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.class_method";
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        //确定是完整的，并且是 class:method, class.method 形式的， 否则会报错
        LuaClassMethodDef psi = (LuaClassMethodDef) node.getPsi();
        LuaClassMethodName classMethodName = psi.getClassMethodName();
        return classMethodName.getNameRef() != null && psi.getFuncBody() != null;
    }

    @Override
    public void serialize(@NotNull LuaClassMethodStub luaClassMethodStub, @NotNull StubOutputStream stubOutputStream) throws IOException {
        stubOutputStream.writeName(luaClassMethodStub.getClassName());
        stubOutputStream.writeName(luaClassMethodStub.getShortName());

        // params
        String[] params = luaClassMethodStub.getParams();
        stubOutputStream.writeByte(params.length);
        for (String param : params) {
            stubOutputStream.writeUTFFast(param);
        }

        //return type set
        LuaTypeSet returnTypeSet = luaClassMethodStub.getReturnType();
        LuaTypeSet.serialize(returnTypeSet, stubOutputStream);

        // is static ?
        stubOutputStream.writeBoolean(luaClassMethodStub.isStatic());
    }

    @NotNull
    @Override
    public LuaClassMethodStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException {
        StringRef className = stubInputStream.readName();
        StringRef shortName = stubInputStream.readName();

        // params
        int len = stubInputStream.readByte();
        String[] params = new String[len];
        for (int i = 0; i < len; i++) {
            params[i] = stubInputStream.readUTFFast();
        }

        LuaTypeSet returnTypeSet = LuaTypeSet.deserialize(stubInputStream);
        boolean isStatic = stubInputStream.readBoolean();
        return new LuaClassMethodStubImpl(StringRef.toString(shortName), StringRef.toString(className), params, returnTypeSet, isStatic, stubElement);
    }

    @Override
    public void indexStub(@NotNull LuaClassMethodStub luaClassMethodStub, @NotNull IndexSink indexSink) {
        String className = luaClassMethodStub.getClassName();
        if (className != null) {
            if (luaClassMethodStub.isStatic()) {
                indexSink.occurrence(LuaClassMethodIndex.KEY, className + ".static");
                indexSink.occurrence(LuaClassMethodIndex.KEY, className + ".static." + luaClassMethodStub.getShortName());
            } else {
                indexSink.occurrence(LuaClassMethodIndex.KEY, className);
            }
        }
    }
}
