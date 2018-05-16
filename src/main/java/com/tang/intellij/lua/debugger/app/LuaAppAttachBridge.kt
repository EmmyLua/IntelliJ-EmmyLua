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
import com.intellij.openapi.util.Key
import com.intellij.xdebugger.XDebugSession
import com.tang.intellij.lua.LuaBundle
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.debugger.attach.DebugMessageId
import com.tang.intellij.lua.debugger.attach.LuaAttachBridgeBase
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcessBase
import com.tang.intellij.lua.debugger.attach.LuaAttachMessage
import com.tang.intellij.lua.psi.LuaFileUtil
import java.nio.charset.Charset

class LuaAppAttachBridge(process: LuaAttachDebugProcessBase, session: XDebugSession)
    : LuaAttachBridgeBase(process, session) {

    private val processListener = object : ProcessListener {

        override fun startNotified(processEvent: ProcessEvent) {

        }

        override fun processTerminated(processEvent: ProcessEvent) {
            onDebugHelperExit(processEvent.exitCode)
            stop()
        }

        override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {

        }

        override fun onTextAvailable(processEvent: ProcessEvent, key: Key<*>) {
            if (key == ProcessOutputTypes.SYSTEM)
                return

            val text = processEvent.text
            if (text.startsWith("port:")) {
                val reg = Regex("port:(\\d+)")
                reg.find(text)?.let {
                    val p = it.groups[1]!!.value.toInt()
                    connect(p)
                }
            }
        }
    }

    fun launch(program: String, configuration: LuaAppRunConfiguration) {
        //this.captureStd = !configuration.showConsole
        val pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory()
        val workingDir = configuration.workingDir
        try {
            if (pluginVirtualDirectory != null) {
                if (workingDir == null || workingDir.isEmpty()) {
                    throw Exception("Working directory not found.")
                }

                // check arch
                val archExe = LuaFileUtil.getArchExeFile()
                val processBuilder = ProcessBuilder(archExe!!)
                val isX86: Boolean
                val archChecker = processBuilder.command(archExe, "arch", "-file", program).start()
                archChecker.waitFor()
                val exitValue = archChecker.exitValue()
                if (exitValue == -1) {
                    throw Exception(String.format("Program [%s] not found.", program))
                }
                isX86 = exitValue == 1

                val archType = if (isX86) "x86" else "x64"
                process.println(LuaBundle.message("run.attach.launch_info", program, archType), LogConsoleType.NORMAL, ConsoleViewContentType.SYSTEM_OUTPUT)
                // attach debugger
                val exe = LuaFileUtil.getPluginVirtualFile(String.format("debugger/windows/%s/emmy.tool.exe", archType))

                val commandLine = GeneralCommandLine(exe!!)
                commandLine.withEnvironment(configuration.envs)
                commandLine.charset = Charset.forName("UTF-8")
                commandLine.addParameters("-m", "run",
                        "--cmd", program,
                        "-e", emmyLua,
                        "-w", workingDir,
                        "--console", if (configuration.showConsole) "true" else "false")
                val args = configuration.parametersArray
                val argString = args.joinToString(" ")
                if (!argString.isEmpty()) {
                    commandLine.addParameters("-a", argString)
                }

                handler = OSProcessHandler(commandLine)
                handler!!.addProcessListener(processListener)
                handler!!.startNotify()
            }
        } catch (e: Exception) {
            process.error(e.message!!, LogConsoleType.EMMY)
            session.stop()
        }
    }

    override fun handleMessage(message: LuaAttachMessage) {
        when (message.id ) {
            DebugMessageId.RespInitialize -> {
                handler?.process?.outputStream?.let {
                    it.write("resume\n".toByteArray())
                    it.flush()
                }
            }
            else -> super.handleMessage(message)
        }
    }
}