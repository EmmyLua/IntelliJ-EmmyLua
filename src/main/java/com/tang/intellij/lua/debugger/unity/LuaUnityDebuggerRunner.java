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

package com.tang.intellij.lua.debugger.unity;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.tang.intellij.lua.debugger.LuaRunner;
import com.tang.intellij.lua.debugger.remote.LuaUnityConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by Taigacon on 2018/11/6.
 */
public class LuaUnityDebuggerRunner extends LuaRunner {

    private static final String ID = "lua.unity.runner";

    @NotNull
    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && (runProfile instanceof LuaUnityConfiguration);
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
                return new LuaUnityDebugProcess(xDebugSession, (LuaUnityConfiguration)environment.getRunProfile());
            }
        });
    }
}
