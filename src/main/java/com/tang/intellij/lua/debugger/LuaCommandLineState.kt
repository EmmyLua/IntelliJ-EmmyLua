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

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView

/**
 *
 * Created by TangZX on 2016/12/30.
 */
class LuaCommandLineState(environment: ExecutionEnvironment) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val runProfile = environment.runProfile as LuaRunConfiguration
        return OSProcessHandler(runProfile.createCommandLine()!!)
    }

    @Throws(ExecutionException::class)
    override fun createConsole(executor: Executor): ConsoleView? {
        val consoleView = super.createConsole(executor)
        consoleView?.addMessageFilter(LuaTracebackFilter(environment.project))
        return consoleView
    }
}
