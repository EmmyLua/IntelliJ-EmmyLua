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
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.stubs.LuaClassDefStub;

/**
 *
 * Created by tangzx on 2016/11/28.
 */
public class LuaClassDefStubImpl extends StubBase<LuaDocClassDef> implements LuaClassDefStub {

    private String className;

    public LuaClassDefStubImpl(String className, StubElement parent) {
        super(parent, LuaElementType.CLASS_DEF);
        this.className = className;
    }

    @Override
    public String getClassName() {
        return className;
    }
}
