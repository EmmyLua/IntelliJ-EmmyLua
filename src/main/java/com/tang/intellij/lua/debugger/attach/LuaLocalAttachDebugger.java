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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessInfo;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.attach.XLocalAttachDebugger;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/2/28.
 */
public class LuaLocalAttachDebugger implements XLocalAttachDebugger {

    private ProcessInfo processInfo;

    LuaLocalAttachDebugger(ProcessInfo processInfo) {

        this.processInfo = processInfo;
    }

    @NotNull
    @Override
    public String getDebuggerDisplayName() {
        return processInfo.getExecutableDisplayName();
    }

    @Override
    public void attachDebugSession(@NotNull Project project, @NotNull ProcessInfo processInfo) throws ExecutionException {

    }
}
