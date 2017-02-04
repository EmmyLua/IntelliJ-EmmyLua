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
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.stubs.LuaClassMethodStub;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodStubImpl extends StubBase<LuaClassMethodDef> implements LuaClassMethodStub {

    private String shortName;
    private String className;
    private String[] params;
    private LuaTypeSet returnTypeSet;
    private boolean isStatic;

    public LuaClassMethodStubImpl(String shortName, String className, String[] params, LuaTypeSet returnTypeSet, boolean isStatic, StubElement parent) {
        super(parent, LuaElementType.CLASS_METHOD_DEF);
        this.shortName = shortName;
        this.className = className;
        this.params = params;
        this.returnTypeSet = returnTypeSet;
        this.isStatic = isStatic;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public LuaTypeSet getReturnType() {
        return returnTypeSet;
    }

    @Override
    public String[] getParams() {
        return params;
    }
}
