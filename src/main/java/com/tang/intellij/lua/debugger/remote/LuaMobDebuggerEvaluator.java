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

package com.tang.intellij.lua.debugger.remote;

import com.intellij.xdebugger.XSourcePosition;
import com.tang.intellij.lua.debugger.LuaDebuggerEvaluator;
import com.tang.intellij.lua.debugger.remote.commands.EvaluatorCommand;
import com.tang.intellij.lua.debugger.remote.value.LuaRValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaMobDebuggerEvaluator extends LuaDebuggerEvaluator {
    private LuaMobDebugProcess process;

    public LuaMobDebuggerEvaluator(@NotNull LuaMobDebugProcess process) {
        this.process = process;
    }

    @Override
    protected void eval(@NotNull String s, @NotNull XEvaluationCallback xEvaluationCallback, @Nullable XSourcePosition xSourcePosition) {
        EvaluatorCommand evaluatorCommand = new EvaluatorCommand("return " + s, data -> {
            Globals standardGlobals = JsePlatform.standardGlobals();
            LuaValue code = standardGlobals.load(data);
            code = code.call();

            String code2Str = code.get(1).toString();
            LuaValue code2 = standardGlobals.load(String.format("local _=%s return _", code2Str));

            LuaRValue value = LuaRValue.Companion.create(s, code2.call(), s, process.getSession());

            xEvaluationCallback.evaluated(value);
        });
        process.runCommand(evaluatorCommand);
    }
}
