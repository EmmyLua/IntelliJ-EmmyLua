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

package com.tang.intellij.lua.debugger.app

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Key
import com.intellij.xdebugger.XDebugSession
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.debugger.remote.LuaMobDebugProcess
import com.tang.intellij.lua.psi.LuaFileUtil

internal class LuaAppMobProcess(session: XDebugSession) : LuaMobDebugProcess(session) {
    private val configuration: LuaAppRunConfiguration = session.runProfile as LuaAppRunConfiguration
    private var isStopped: Boolean = false

    override fun sessionInitialized() {
        super.sessionInitialized()
        val setupPackagePath = StringBuilder(String.format("%s/?.lua;", LuaFileUtil.getPluginVirtualFile("debugger/mobdebug")))

        val modules = ModuleManager.getInstance(session.project).modules
        for (module in modules) {
            val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots
            for (sourceRoot in sourceRoots) {
                val path = sourceRoot.canonicalPath
                if (path != null) {
                    setupPackagePath.append(path).append("/?.lua;")
                }
            }
        }

        val commandLine = GeneralCommandLine(configuration.program)
        commandLine.addParameters("-e", String.format("package.path = package.path .. ';%s' require('mobdebug').start()", setupPackagePath.toString()))
        commandLine.addParameters(configuration.file!!)

        val dir = configuration.workingDir
        if (dir != null && !dir.isEmpty())
            commandLine.setWorkDirectory(dir)

        try {
            val handler = OSProcessHandler(commandLine)
            handler.addProcessListener(object : ProcessListener {
                override fun startNotified(processEvent: ProcessEvent) {

                }

                override fun processTerminated(processEvent: ProcessEvent) {
                    if (!isStopped)
                        session.stop()
                }

                override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {

                }

                override fun onTextAvailable(processEvent: ProcessEvent, key: Key<*>) {
                    if (key === ProcessOutputTypes.STDOUT) {
                        print(processEvent.text, LogConsoleType.NORMAL, ConsoleViewContentType.NORMAL_OUTPUT)
                    } else if (key === ProcessOutputTypes.STDERR) {
                        error(processEvent.text)
                    }
                }
            })
            handler.startNotify()
        } catch (e: Exception) {
            e.message?.let { error(it) }
            session.stop()
        }
    }

    override fun stop() {
        isStopped = true
        super.stop()
    }
}
