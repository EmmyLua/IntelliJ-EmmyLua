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

package com.tang.intellij.lua.debugger

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.GenericProgramRunner

/**
 * lua runner
 * Created by tangzx on 2017/6/10.
 */
abstract class LuaRunner : GenericProgramRunner<RunnerSettings>() {
    override fun canRun(executorId: String, runProfile: RunProfile): Boolean {
        return DefaultDebugExecutor.EXECUTOR_ID == executorId || DefaultRunExecutor.EXECUTOR_ID == executorId
    }
}
