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
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import com.intellij.util.Processor
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.ui.XDebugTabLayouter
import com.tang.intellij.lua.debugger.LuaDebugProcess
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider
import com.tang.intellij.lua.debugger.attach.vfs.MemoryDataVirtualFile
import com.tang.intellij.lua.debugger.attach.vfs.MemoryFileSystem
import com.tang.intellij.lua.debugger.attach.vfs.MemoryVirtualFile
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.LuaFileUtil
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * Created by tangzx on 2017/3/26.
 */
abstract class LuaAttachDebugProcessBase protected constructor(session: XDebugSession)
    : LuaDebugProcess(session), LuaAttachBridgeBase.ProtoHandler {
    lateinit var bridge: LuaAttachBridgeBase
    private val editorsProvider: LuaDebuggerEditorsProvider
    private val loadedScriptMap = ConcurrentHashMap<Int, LoadedScript>()
    private lateinit var vmPanel: LuaVMPanel
    private lateinit var memoryFilesPanel: MemoryFilesPanel
    private lateinit var profilerPanel: ProfilerPanel
    private var toggleProfiler: Boolean = false
    private val memoryFileSystem = MemoryFileSystem.instance

    init {
        session.setPauseActionSupported(false)
        editorsProvider = LuaDebuggerEditorsProvider()
    }

    protected abstract fun startBridge(): LuaAttachBridgeBase

    override fun sessionInitialized() {
        super.sessionInitialized()
        bridge = startBridge()
        with(ApplicationManager.getApplication()) {
            invokeLater {
                runWriteAction {
                    //remove breakpoints
                    val manager = XDebuggerManager.getInstance(session.project)
                    manager.breakpointManager.allBreakpoints.forEach {
                        if (it is XLineBreakpoint) {
                            if (it.fileUrl.startsWith(MemoryFileSystem.PROTOCOL)) {
                                manager.breakpointManager.removeBreakpoint(it)
                            }
                        }
                    }
                    //close file editor
                    val fileEditorManager = FileEditorManager.getInstance(session.project)
                    fileEditorManager.openFiles.forEach {
                        if (it is MemoryVirtualFile) {
                            fileEditorManager.closeFile(it)
                        }
                    }
                    //clear memory files
                    memoryFileSystem.clear()

                    memoryFilesPanel.clear()
                }
            }
        }
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

    var profilerState: Boolean
        set(value) {
            val dm = if (value) LuaAttachMessage(DebugMessageId.ReqProfilerBegin)
            else LuaAttachMessage(DebugMessageId.ReqProfilerEnd)
            bridge.send(dm)
        }
        get() = toggleProfiler

    override fun handle(message: LuaAttachMessage) {
        when (message) {
            is DMLoadScript -> with(ApplicationManager.getApplication()) { invokeLater { runWriteAction {
                try {
                    onLoadScript(message)
                } finally {
                    bridge.sendDone()
                }
            } }}
            is DMBreak -> onBreak(message)
            is DMException -> message.print()
            is DMMessage -> message.print()
            is DMRespProfilerData -> onProfilerData(message)
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
                    DebugMessageId.RespProfilerBegin -> toggleProfiler = true
                    DebugMessageId.RespProfilerEnd -> toggleProfiler = false
                    else -> {
                        println("unknown message : ${message.id}")
                    }
                }
            }
        }
    }

    private fun onProfilerData(message: DMRespProfilerData) {
        profilerPanel.updateProfiler(message)
    }

    fun findFile(name: String?): VirtualFile? {
        if (name == null)
            return null
        val path = PathUtil.getCanonicalPath(name)

        val f = memoryFileSystem.findMemoryFile(path)
        if (f != null)
            return f
        return LuaFileUtil.findFile(session.project, path)
    }

    private fun onBreak(proto: DMBreak) {
        val file = findFile(proto.name)
        if (file == null) {
            bridge.sendRun()
            return
        }

        setStack(proto.stack)
    }

    private fun onLoadScript(proto: DMLoadScript) {
        // remove exist
        for (entry in loadedScriptMap) {
            if (entry.value.name == proto.fileName) {
                loadedScriptMap.remove(entry.key)
                break
            }
        }

        var file = findFile(proto.fileName)
        if (file == null) {
            if (proto.state != CodeState.Unavailable) {
                file = createMemoryFile(proto)
                print("[✔] Load memory file : ", ConsoleViewContentType.SYSTEM_OUTPUT)
                val virtualFile = file
                session.consoleView.printHyperlink(proto.fileName) {
                    FileEditorManager.getInstance(it).openFile(virtualFile, true)
                }
                print("\n", ConsoleViewContentType.SYSTEM_OUTPUT)
            }
        } else {
            if (file is MemoryDataVirtualFile) {
                file.setBinaryContent(proto.source.toByteArray())
                file.state = proto.state
            }
            print("[✔] File was loaded :", ConsoleViewContentType.SYSTEM_OUTPUT)
            val virtualFile = file
            session.consoleView.printHyperlink(proto.fileName) {
                FileEditorManager.getInstance(it).openFile(virtualFile, true)
            }
            print("\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        }

        if (file != null) {
            val script = LoadedScript(file, proto.index, proto.fileName, proto.state)
            loadedScriptMap.put(proto.index, script)

            processBreakpoint(Processor {
                val pos = it.sourcePosition
                if (pos != null) {
                    if (LuaFileUtil.fileEquals(file, pos.file)) {
                        bridge.addBreakpoint(proto.index, it)
                    }
                }
                true
            })
        } else {
            print("[✘] File not found ", ConsoleViewContentType.SYSTEM_OUTPUT)
            session.consoleView.printHyperlink("[TRY MEMORY FILE]") {
                bridge.send(DMReqReloadScript(proto.index))
            }
            print(": ${proto.fileName}\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    private fun createMemoryFile(dm: DMLoadScript): VirtualFile {
        val childFile = memoryFileSystem.createChildFile(this, memoryFileSystem.getRoot(), dm.fileName) as MemoryDataVirtualFile
        childFile.index = dm.index
        childFile.state = dm.state
        childFile.setBinaryContent(dm.source.toByteArray())
        memoryFilesPanel.addFile(childFile)
        return childFile
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

    override fun registerAdditionalActions(leftToolbar: DefaultActionGroup, topToolbar: DefaultActionGroup, settings: DefaultActionGroup) {
        super.registerAdditionalActions(leftToolbar, topToolbar, settings)
        topToolbar.add(object : ToggleAction("Lua profiler!", null, LuaIcons.Debugger.Actions.PROFILER) {
            override fun isSelected(event: AnActionEvent): Boolean {
                return profilerState
            }

            override fun setSelected(event: AnActionEvent, state: Boolean) {
                profilerState = state
            }
        })
    }

    override fun createTabLayouter(): XDebugTabLayouter {
        return object : XDebugTabLayouter() {
            override fun registerAdditionalContent(ui: RunnerLayoutUi) {
                super.registerAdditionalContent(ui)
                createVMPanel(ui)
                createMemoryFilesPanel(ui)
                createProfilerPanel(ui)
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
        memoryFilesPanel = MemoryFilesPanel(session.project, this)
        val content = ui.createContent(DebuggerContentInfo.FRAME_CONTENT, memoryFilesPanel, "Memory files", AllIcons.Debugger.Frame, null)
        content.isCloseable = false
        ui.addContent(content, 0, PlaceInGrid.left, false)
    }

    private fun createProfilerPanel(ui: RunnerLayoutUi) {
        profilerPanel = ProfilerPanel(this)
        val content = ui.createContent(DebuggerContentInfo.FRAME_CONTENT, profilerPanel, "Profiler", AllIcons.Debugger.Frame, null)
        content.isCloseable = false
        ui.addContent(content, 0, PlaceInGrid.left, false)
    }
}