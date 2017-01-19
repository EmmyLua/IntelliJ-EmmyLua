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
import com.intellij.util.io.StringRef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;
import com.tang.intellij.lua.psi.LuaElementType;
import com.tang.intellij.lua.stubs.LuaDocClassFieldStub;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaClassFieldStubImpl extends StubBase<LuaDocFieldDef> implements LuaDocClassFieldStub {
    private String name;
    private StringRef className;

    public LuaClassFieldStubImpl(StubElement parent, String name, StringRef className) {
        super(parent, LuaElementType.CLASS_FIELD_DEF);
        this.name = name;
        this.className = className;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StringRef getClassName() {
        return className;
    }
}
