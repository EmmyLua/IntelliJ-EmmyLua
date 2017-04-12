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

package com.tang.intellij.lua.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.psi.LuaNameExpr;
import com.tang.intellij.lua.psi.LuaPsiResolveUtil;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaNameStub;
import com.tang.intellij.lua.stubs.types.LuaNameType;

/**
 *
 * Created by TangZX on 2017/4/12.
 */
public class LuaNameStubImpl extends StubBase<LuaNameExpr> implements LuaNameStub {

    private LuaNameExpr nameExpr;
    private String name;

    public LuaNameStubImpl(LuaNameExpr luaNameExpr, StubElement parent, LuaNameType elementType) {
        super(parent, elementType);
        nameExpr = luaNameExpr;
        name = luaNameExpr.getName();
    }

    public LuaNameStubImpl(String name, StubElement stubElement, LuaNameType luaNameType) {
        super(stubElement, luaNameType);
        this.name = name;
    }

    private boolean checkGlobal() {
        SearchContext context = new SearchContext(nameExpr.getProject());
        context.setCurrentStubFile(nameExpr.getContainingFile());
        return LuaPsiResolveUtil.resolveLocal(nameExpr, context) == null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isGlobal() {
        return checkGlobal();
    }
}
