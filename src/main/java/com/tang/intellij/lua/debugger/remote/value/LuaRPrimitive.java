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

import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.*;

/**
 *
 * Created by tangzx on 2017/4/16.
 */
public class LuaRPrimitive extends LuaRValue {
    private String type;
    private String data;

    LuaRPrimitive(@NotNull String name) {
        super(name);
    }

    @Override
    protected void parse(LuaValue data, String desc) {
        this.data = data.toString();
        if (data instanceof LuaString)
            type = "string";
        else if (data instanceof LuaNumber)
            type = "number";
        else if (data instanceof LuaBoolean)
            type = "boolean";
        else if (data instanceof LuaFunction)
            type = "function";
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(null, type, data, false);
    }
}
