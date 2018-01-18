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

package com.tang.intellij.lua.debugger.remote

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.util.Processor
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XSuspendContext
import com.tang.intellij.lua.debugger.IRemoteConfiguration
import com.tang.intellij.lua.debugger.LuaDebugProcess
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider
import com.tang.intellij.lua.debugger.remote.commands.DebugCommand
import com.tang.intellij.lua.debugger.remote.commands.GetStackCommand
import com.tang.intellij.lua.psi.LuaFileUtil
import java.io.IOException

/**

 * Created by TangZX on 2016/12/30.
 */
open class LuaMobDebugProcess(session: XDebugSession) : LuaDebugProcess(session), MobServerListener {

    private val runProfile: IRemoteConfiguration = session.runProfile as IRemoteConfiguration
    private val editorsProvider: LuaDebuggerEditorsProvider = LuaDebuggerEditorsProvider()
    private val mobServer: MobServer = MobServer(this)
    private var mobClient: MobClient? = null

    override fun getEditorsProvider(): XDebuggerEditorsProvider {
        return editorsProvider
    }

    override fun sessionInitialized() {
        super.sessionInitialized()

        try {
            println("Start mobdebug server at port:" + runProfile.port, ConsoleViewContentType.SYSTEM_OUTPUT)
            println("Waiting for process connection...", ConsoleViewContentType.SYSTEM_OUTPUT)
            mobServer.start(runProfile.port)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        mobServer.stop()
    }

    override fun run() {
        mobClient?.addCommand("RUN")
    }

    override fun startStepOver(context: XSuspendContext?) {
        mobClient?.addCommand("OVER")
    }

    override fun startStepInto(context: XSuspendContext?) {
        mobClient?.addCommand("STEP")
    }

    override fun startStepOut(context: XSuspendContext?) {
        mobClient?.addCommand("OUT")
    }

    private fun sendBreakpoint(client: MobClient, sourcePosition: XSourcePosition) {
        val project = session.project
        val file = sourcePosition.file
        val fileShortUrl: String? = LuaFileUtil.getShortPath(project, file)
        if (fileShortUrl != null) {
            LuaFileUtil.getAllAvailablePathsForMob(fileShortUrl, file).forEach{ url ->
                client.sendAddBreakpoint(url, sourcePosition.line + 1)
            }
        }
    }

    override fun registerBreakpoint(sourcePosition: XSourcePosition, breakpoint: XLineBreakpoint<*>) {
        super.registerBreakpoint(sourcePosition, breakpoint)
        if (mobClient != null) sendBreakpoint(mobClient!!, sourcePosition)
    }

    override fun unregisterBreakpoint(sourcePosition: XSourcePosition, breakpoint: XLineBreakpoint<*>) {
        super.unregisterBreakpoint(sourcePosition, breakpoint)
        if (mobClient != null) {
            val file = sourcePosition.file
            val fileShortUrl = LuaFileUtil.getShortPath(session.project, file)
            LuaFileUtil.getAllAvailablePathsForMob(fileShortUrl, file).forEach{ url ->
                mobClient!!.sendRemoveBreakpoint(url, sourcePosition.line + 1)
            }
        }
    }

    override fun handleResp(client: MobClient, code: Int, data: String?) {
        when (code) {
            202 -> runCommand(GetStackCommand())
        }
    }

    override fun onDisconnect(client: MobClient) {
        mobServer.restart()
    }

    override fun onConnect(client: MobClient) {
        mobClient = client
        processBreakpoint(Processor { bp ->
            bp.sourcePosition?.let { sendBreakpoint(client, it) }
            true
        })
        client.addCommand("RUN")
    }

    override val process: LuaMobDebugProcess
        get() = this

    fun runCommand(command: DebugCommand) {
        mobClient?.addCommand(command)
    }
}
