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

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.LuaParamInfo;
import com.tang.intellij.lua.psi.LuaTypes;
import com.tang.intellij.lua.stubs.LuaGlobalFuncStub;

/**
 *
 * Created by tangzx on 2016/11/26.
 */
public class LuaGlobalFuncStubImpl extends StubBase<LuaGlobalFuncDef> implements LuaGlobalFuncStub {

    private String funcName;
    private LuaParamInfo[] params;
    private LuaTypeSet returnTypeSet;

    public LuaGlobalFuncStubImpl(String funcName, LuaParamInfo[] params, LuaTypeSet returnTypeSet, StubElement parent) {
        super(parent, (IStubElementType) LuaTypes.GLOBAL_FUNC_DEF);
        this.funcName = funcName;
        this.params = params;
        this.returnTypeSet = returnTypeSet;
    }

    @Override
    public String getName() {
        return funcName;
    }

    @Override
    public LuaTypeSet getReturnType() {
        return returnTypeSet;
    }

    @Override
    public LuaParamInfo[] getParams() {
        return params;
    }
}