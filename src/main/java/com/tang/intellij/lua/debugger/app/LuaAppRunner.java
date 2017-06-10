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

package com.tang.intellij.lua.debugger.app;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.tang.intellij.lua.debugger.DebuggerType;
import com.tang.intellij.lua.debugger.LuaRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/5/7.
 */
public class LuaAppRunner extends LuaRunner {
    private static final String ID = "lua.app.runner";
    @NotNull
    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String s, @NotNull RunProfile runProfile) {
        return runProfile instanceof LuaAppRunConfiguration && super.canRun(s, runProfile);
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        XDebugSession session = createSession(state, environment);
        return session.getRunContentDescriptor();
    }

    private XDebugSession createSession(RunProfileState state, ExecutionEnvironment environment) throws ExecutionException {
        XDebuggerManager manager = XDebuggerManager.getInstance(environment.getProject());
        return manager.startSession(environment, new XDebugProcessStarter() {
            @NotNull
            @Override
            public XDebugProcess start(@NotNull XDebugSession xDebugSession) throws ExecutionException {
                LuaAppRunConfiguration configuration = (LuaAppRunConfiguration) environment.getRunProfile();
                DebuggerType debuggerType = configuration.getDebuggerType();
                if (debuggerType == DebuggerType.Mob)
                    return new LuaAppMobProcess(xDebugSession);
                else if (debuggerType == DebuggerType.Attach)
                    return new LuaAppAttachProcess(xDebugSession);
                return new LuaAppMobProcess(xDebugSession);
            }
        });
    }
}
