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

import com.intellij.debugger.ui.DebuggerContentInfo
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.components.JBList
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.ui.XDebugTabLayouter
import com.tang.intellij.lua.debugger.LuaDebugProcess
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.LuaFileUtil
import java.awt.BorderLayout
import java.util.concurrent.ConcurrentHashMap
import javax.swing.DefaultListModel
import javax.swing.JPanel

/**
 *
 * Created by tangzx on 2017/3/26.
 */
abstract class LuaAttachDebugProcess protected constructor(session: XDebugSession)
    : LuaDebugProcess(session), LuaAttachBridgeBase.ProtoHandler {
    private val editorsProvider: LuaDebuggerEditorsProvider
    private val loadedScriptMap = ConcurrentHashMap<Int, LoadedScript>()
    lateinit var bridge: LuaAttachBridgeBase
    private lateinit var vmPanel: LuaVMPanel
    private lateinit var memoryFilesPanel: MemoryFilesPanel

    init {
        session.setPauseActionSupported(false)
        editorsProvider = LuaDebuggerEditorsProvider()
    }

    protected abstract fun startBridge(): LuaAttachBridgeBase

    override fun sessionInitialized() {
        super.sessionInitialized()
        bridge = startBridge()
    }

    override fun getEditorsProvider(): XDebuggerEditorsProvider {
        return editorsProvider
    }

    override fun startStepOver(context: XSuspendContext?) {
        bridge.send(LuaAttachMessage(DebugMessageId.StepOver))
    }

    override fun startStepInto(context: XSuspendContext?) {
        bridge.send(LuaAttachMessage(DebugMessageId.StepInto))
    }

    override fun startStepOut(context: XSuspendContext?) {
        bridge.send(LuaAttachMessage(DebugMessageId.StepOut))
    }

    override fun stop() {
        bridge.stop()
    }

    override fun run() {
        bridge.sendRun()
    }

    override fun handle(message: LuaAttachMessage) {
        when (message) {
            is DMLoadScript -> onLoadScript(message)
            is DMBreak -> onBreak(message)
            is DMException -> message.print()
            is DMMessage -> message.print()
            else -> {
                when (message.id) {
                    DebugMessageId.SessionEnd -> {
                        bridge.stop()
                        session.stop()
                    }
                    DebugMessageId.CreateVM -> {
                        vmPanel.addVM(message)
                    }
                    DebugMessageId.DestroyVM -> {
                        vmPanel.removeVM(message)
                    }
                    else -> {
                        println("unknown message : ${message.id}")
                    }
                }
            }
        }
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
        var file = LuaFileUtil.findFile(session.project, proto.fileName)
        if (file == null) {
            file = createMemoryFile(proto)
            //print(String.format("[✘] File not found : %s\n", proto.fileName), ConsoleViewContentType.SYSTEM_OUTPUT)
            print(String.format("[✔] Create memory file : %s\n", proto.fileName), ConsoleViewContentType.SYSTEM_OUTPUT)
        }

        val script = LoadedScript(file, proto.index, proto.fileName)
        loadedScriptMap.put(proto.index, script)
        //print(String.format("[✔] File was loaded : %s\n", proto.fileName), ConsoleViewContentType.SYSTEM_OUTPUT)

        for (pos in registeredBreakpoints.keys) {
            if (LuaFileUtil.fileEquals(file, pos.file)) {
                val breakpoint = registeredBreakpoints[pos]!!
                bridge.addBreakpoint(proto.index, breakpoint)
            }
        }
        bridge.sendDone()
    }

    private fun createMemoryFile(dm: DMLoadScript): VirtualFile {
        val file = LightVirtualFile(dm.fileName, LuaFileType.INSTANCE, dm.source)
        file.isWritable = false
        memoryFilesPanel.addFile(file)
        ApplicationManager.getApplication().invokeLater {
            FileEditorManagerEx.getInstance(session.project).openFile(file, true)
        }
        return file
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

    override fun createTabLayouter(): XDebugTabLayouter {
        return object : XDebugTabLayouter() {
            override fun registerAdditionalContent(ui: RunnerLayoutUi) {
                super.registerAdditionalContent(ui)
                createVMPanel(ui)
                createMemoryFilesPanel(ui)
            }
        }
    }

    private fun createVMPanel(ui: RunnerLayoutUi) {
        vmPanel = LuaVMPanel(session.project)
        val content = ui.createContent(DebuggerContentInfo.FRAME_CONTENT, vmPanel, "Lua VM", AllIcons.Debugger.Frame, null)
        content.isCloseable = false
        ui.addContent(content, 0, PlaceInGrid.left, false)
    }

    private fun createMemoryFilesPanel(ui: RunnerLayoutUi) {
        memoryFilesPanel = MemoryFilesPanel(session.project)
        val content = ui.createContent(DebuggerContentInfo.FRAME_CONTENT, memoryFilesPanel, "Memory files", AllIcons.Debugger.Frame, null)
        content.isCloseable = false
        ui.addContent(content, 0, PlaceInGrid.left, false)
    }
}

class LuaVMPanel(val project: Project) : JPanel(BorderLayout()) {
    private val model = DefaultListModel<String>()

    init {
        val list = JBList<String>(model)
        add(list, BorderLayout.CENTER)
    }

    fun addVM(message: LuaAttachMessage) {
        model.addElement("0x${java.lang.Long.toHexString(message.L)}")
    }

    fun removeVM(message: LuaAttachMessage) {

    }
}
