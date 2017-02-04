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
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.stubs.LuaDocClassFieldStub;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaDocClassFieldStubImpl extends StubBase<LuaDocFieldDef> implements LuaDocClassFieldStub {
    private String name;
    private String className;
    private LuaTypeSet type;

    public LuaDocClassFieldStubImpl(StubElement parent, String name, String className, LuaTypeSet type) {
        super(parent, LuaElementType.CLASS_FIELD_DEF);
        this.name = name;
        this.className = className;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LuaTypeSet getType() {
        return type;
    }

    @Override
    public String getClassName() {
        return className;
    }
}
