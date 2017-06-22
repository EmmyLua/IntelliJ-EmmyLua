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

package com.tang.intellij.lua.debugger.remote.value;

import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XNavigatable;
import com.tang.intellij.lua.debugger.attach.value.LuaXValue;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;

/**
 * remote value
 * Created by tangzx on 2017/4/16.
 */
public abstract class LuaRValue extends XNamedValue {

    protected XDebugSession session;

    LuaRValue(@NotNull String name) {
        super(name);
    }

    protected abstract void parse(LuaValue data, String desc);

    public static LuaRValue create(String name, LuaValue data, String desc, XDebugSession session) {
        LuaRValue value = null;

        if (data.istable()) {
            value = new LuaRTable(name);
        } else if (data.isboolean() || data.isnumber() || data.isstring()) {
            value = new LuaRPrimitive(name);
        } else if (data.isnil()) {
            value = new LuaRPrimitive(name);
            desc = "nil";
        } else if (data.isfunction()) {
            value = new LuaRFunction(name);
        }
        if (value != null) {
            value.session = session;
            value.parse(data, desc);
        }
        return value;
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable xNavigatable) {
        if (session != null) {
            LuaXValue.computeSourcePosition(xNavigatable, getName(), session);
        }
    }
}
