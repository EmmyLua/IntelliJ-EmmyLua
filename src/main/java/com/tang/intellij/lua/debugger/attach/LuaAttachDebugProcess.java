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

import com.intellij.execution.process.ProcessInfo;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/3/26.
 */
public class LuaAttachDebugProcess extends XDebugProcess {
    private LuaDebuggerEditorsProvider editorsProvider;
    private ProcessInfo processInfo;
    private LuaAttachBridge bridge;

    LuaAttachDebugProcess(@NotNull XDebugSession session, ProcessInfo processInfo) {
        super(session);
        this.processInfo = processInfo;
        editorsProvider = new LuaDebuggerEditorsProvider();
        bridge = new LuaAttachBridge(processInfo);
        bridge.start();
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        super.startStepOver(context);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        super.startStepInto(context);
    }

    @Override
    public void stop() {
        bridge.stop();
    }
}
