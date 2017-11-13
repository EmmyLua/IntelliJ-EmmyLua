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

package com.tang.intellij.lua.debugger.attach;

import com.intellij.xdebugger.XSourcePosition;
import com.tang.intellij.lua.debugger.LuaDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public class LuaAttachDebuggerEvaluator extends LuaDebuggerEvaluator {
    private LuaAttachDebugProcessBase process;
    private LuaAttachStackFrame stackFrame;

    LuaAttachDebuggerEvaluator(LuaAttachDebugProcessBase process, LuaAttachStackFrame stackFrame) {
        this.process = process;
        this.stackFrame = stackFrame;
    }

    @Override
    protected void eval(@NotNull String express, @NotNull XEvaluationCallback xEvaluationCallback, @Nullable XSourcePosition xSourcePosition) {
        process.getBridge().eval(stackFrame.getProto().getL(), express, stackFrame.getStack(), 1, result -> {
            if (result.getSuccess()) {
                xEvaluationCallback.evaluated(result.getResultNode().getValue());
            } else {
                xEvaluationCallback.errorOccurred("error");
            }
        });
    }
}
