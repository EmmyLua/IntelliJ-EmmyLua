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

package com.tang.intellij.lua.debugger.attach

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XSuspendContext
import com.tang.intellij.lua.debugger.LuaDebugProcess
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider
import com.tang.intellij.lua.psi.LuaFileUtil
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * Created by tangzx on 2017/3/26.
 */
abstract class LuaAttachDebugProcess protected constructor(session: XDebugSession) : LuaDebugProcess(session), LuaAttachBridge.ProtoHandler {
    private val editorsProvider: LuaDebuggerEditorsProvider
    lateinit var bridge: LuaAttachBridge
    private val loadedScriptMap = ConcurrentHashMap<Int, LoadedScript>()

    init {
        session.setPauseActionSupported(false)
        editorsProvider = LuaDebuggerEditorsProvider()
    }

    protected abstract fun startBridge(): LuaAttachBridge

    override fun sessionInitialized() {
        super.sessionInitialized()
        bridge = startBridge()
    }

    override fun getEditorsProvider(): XDebuggerEditorsProvider {
        return editorsProvider
    }

    override fun startStepOver(context: XSuspendContext?) {
        bridge.send(LuaAttachMessage(DebugMessageId.StepOver))//"stepover"
    }

    override fun startStepInto(context: XSuspendContext?) {
        bridge.send(LuaAttachMessage(DebugMessageId.StepInto))//bridge.send("stepinto")
    }

    override fun startStepOut(context: XSuspendContext?) {
        bridge.send(LuaAttachMessage(DebugMessageId.StepOut))//bridge.send("stepout")
    }

    override fun stop() {
        bridge.stop()
    }

    override fun run() {
        bridge.sendRun()
    }

    override fun handle(proto: LuaAttachMessage) {
        when (proto) {
            is DMLoadScript -> {
                onLoadScript(proto)
            }
            is DMBreak -> {
                onBreak(proto)
            }
            is DMMessage -> {

            }
        }
        /*val type = proto.type
        when (type) {
            LuaAttachProto.Exception, LuaAttachProto.Message -> {
                val messageProto = proto as LuaAttachMessageProto
                messageProto.outputToConsole()
            }
            LuaAttachProto.LoadScript -> {
                val loadScriptProto = proto as LuaAttachLoadScriptProto
                onLoadScript(loadScriptProto)
            }
            LuaAttachProto.Break -> onBreak(proto as LuaAttachBreakProto)
            LuaAttachProto.SessionEnd, LuaAttachProto.DestroyVM -> {
                bridge.stop(false)
                session.stop()
            }
        }*/
    }

    private fun onBreak(proto: DMBreak) {
        val file = LuaFileUtil.findFile(session.project, proto.name)
        if (file == null) {
            bridge.sendRun()
            return
        }

        setStack(proto.stack)
    }

    private fun onLoadScript(proto: DMLoadScript) {
        val file = LuaFileUtil.findFile(session.project, proto.fileName)
        if (file == null) {
            print(String.format("[✘] File not found : %s\n", proto.fileName), ConsoleViewContentType.SYSTEM_OUTPUT)
        } else {
            val script = LoadedScript(file, proto.index, proto.fileName)
            loadedScriptMap.put(proto.index, script)
            print(String.format("[✔] File was loaded : %s\n", proto.fileName), ConsoleViewContentType.SYSTEM_OUTPUT)

            for (pos in registeredBreakpoints.keys) {
                if (LuaFileUtil.fileEquals(file, pos.file)) {
                    val breakpoint = registeredBreakpoints[pos]!!
                    bridge.addBreakpoint(proto.index, breakpoint)
                }
            }
        }
        bridge.sendDone()
    }

    override fun registerBreakpoint(sourcePosition: XSourcePosition, breakpoint: XLineBreakpoint<*>) {
        super.registerBreakpoint(sourcePosition, breakpoint)
        for (script in loadedScriptMap.values) {
            if (LuaFileUtil.fileEquals(sourcePosition.file, script.file)) {
                bridge.addBreakpoint(script.index, breakpoint)
                break
            }
        }
    }

    override fun unregisterBreakpoint(sourcePosition: XSourcePosition, breakpoint: XLineBreakpoint<*>) {
        super.unregisterBreakpoint(sourcePosition, breakpoint)
        val sourceFile = sourcePosition.file

        for (script in loadedScriptMap.values) {
            val scriptFile = script.file
            if (LuaFileUtil.fileEquals(sourceFile, scriptFile)) {
                bridge.removeBreakpoint(script.index, breakpoint)
                break
            }
        }
    }

    fun getScript(index: Int): LoadedScript? {
        return loadedScriptMap[index]
    }
}
