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

package com.tang.intellij.lua.debugger.emmyAttach

import com.google.gson.Gson
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.util.Key
import com.intellij.xdebugger.XDebugSession
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.debugger.emmy.*
import com.tang.intellij.lua.psi.LuaFileUtil

class EmmyAttachDebugProcess(session: XDebugSession, private val processInfo: ProcessInfo) : EmmyDebugProcessBase(session) {
    override fun setupTransporter() {
        val suc = attach()
        if (!suc) {
            session.stop()
            return
        }
        var port = processInfo.pid
        // 1024 - 65535
        while (port > 0xffff) port -= 0xffff
        while (port < 0x400) port += 0x400

        val transporter = SocketClientTransporter("localhost", port)
        transporter.handler = this
        transporter.logger = this
        this.transporter = transporter
        transporter.start()
    }

    private fun detectArch(pid: Int): EmmyWinArch {
        val tool = LuaFileUtil.getPluginVirtualFile("debugger/emmy/windows/x86/emmy_tool.exe")
        val commandLine = GeneralCommandLine(tool)
        commandLine.addParameters("arch_pid", "$pid")
        val process = commandLine.createProcess()
        process.waitFor()
        val exitValue = process.exitValue()
        return if (exitValue == 0) EmmyWinArch.X64 else EmmyWinArch.X86
    }

    private fun attach(): Boolean {
        val arch = detectArch(processInfo.pid)
        val path = LuaFileUtil.getPluginVirtualFile("debugger/emmy/windows/${arch}")
        val commandLine = GeneralCommandLine("${path}/emmy_tool.exe")
        commandLine.addParameters(
                "attach",
                "-p",
                "${processInfo.pid}",
                "-dir",
                path,
                "-dll",
                "emmy_hook.dll"
        )
        val handler = OSProcessHandler(commandLine)
        handler.addProcessListener(object : ProcessListener {
            override fun startNotified(processEvent: ProcessEvent) {
            }

            override fun processTerminated(processEvent: ProcessEvent) {
            }

            override fun processWillTerminate(processEvent: ProcessEvent, b: Boolean) {
            }

            override fun onTextAvailable(processEvent: ProcessEvent, key: Key<*>) {
                when (key) {
                    ProcessOutputTypes.STDERR -> print(processEvent.text, LogConsoleType.NORMAL, ConsoleViewContentType.ERROR_OUTPUT)
                    ProcessOutputTypes.STDOUT -> print(processEvent.text, LogConsoleType.NORMAL, ConsoleViewContentType.SYSTEM_OUTPUT)
                }
            }
        })
        handler.startNotify()
        handler.waitFor()
        return handler.exitCode == 0
    }

    override fun onReceiveMessage(cmd: MessageCMD, json: String) {
        if (cmd == MessageCMD.AttachedNotify) {
            val msg = Gson().fromJson(json, AttachedNotify::class.java)
            println("Attached to lua state 0x${msg.state.toString(16)}", LogConsoleType.NORMAL, ConsoleViewContentType.SYSTEM_OUTPUT)
        }
        else super.onReceiveMessage(cmd, json)
    }
}