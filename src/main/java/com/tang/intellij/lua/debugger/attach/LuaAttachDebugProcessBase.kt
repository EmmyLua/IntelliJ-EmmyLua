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
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleState
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PathUtil
import com.intellij.util.Processor
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.ui.XDebugTabLayouter
import com.tang.intellij.lua.debugger.LogConsoleType
import com.tang.intellij.lua.debugger.LuaDebugProcess
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider
import com.tang.intellij.lua.debugger.attach.vfs.MemoryDataVirtualFile
import com.tang.intellij.lua.debugger.attach.vfs.MemoryFileSystem
import com.tang.intellij.lua.debugger.attach.vfs.MemoryVirtualFile
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.LuaFileUtil
import java.nio.charset.Charset
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
    protected var emmyInputEnabled: Boolean = false
    private var emmyConsole: ConsoleView? = null

    init {
        session.setPauseActionSupported(false)
        editorsProvider = LuaDebuggerEditorsProvider()
    }

    /**
     * stdout/stderr
     */
    open val charset: Charset get() = LuaSettings.instance.attachDebugDefaultCharset

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
            is DMMessage -> handleLogMessage(message)
            is DMRespProfilerData -> onProfilerData(message)
            is DMLoadError -> onLoadError(message)
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

    private var stdoutIncompleteString = IncompleteString()
    private var stderrIncompleteString = IncompleteString()

    private fun handleLogMessage(message: DMMessage) {
        when (message.type) {
            DMMessage.Stdout -> {
                stdoutIncompleteString.append(message.bytes)
                val text = stdoutIncompleteString.decode(charset)
                print(text, LogConsoleType.NORMAL, ConsoleViewContentType.NORMAL_OUTPUT)
            }
            DMMessage.Stderr -> {
                stderrIncompleteString.append(message.bytes)
                val text = stderrIncompleteString.decode(charset)
                print(text, LogConsoleType.NORMAL, ConsoleViewContentType.ERROR_OUTPUT)
            }
            else -> message.print()
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
                print("[✔] Load memory file : ", LogConsoleType.EMMY, ConsoleViewContentType.SYSTEM_OUTPUT)
                val virtualFile = file
                printHyperlink(proto.fileName, LogConsoleType.EMMY) {
                    FileEditorManager.getInstance(it).openFile(virtualFile, true)
                }
                print("\n", LogConsoleType.EMMY, ConsoleViewContentType.SYSTEM_OUTPUT)
            }
        } else {
            if (file is MemoryDataVirtualFile) {
                file.setBinaryContent(proto.source.toByteArray())
                file.state = proto.state
            }
            print("[✔] File was loaded :", LogConsoleType.EMMY, ConsoleViewContentType.SYSTEM_OUTPUT)
            val virtualFile = file
            printHyperlink(proto.fileName, LogConsoleType.EMMY) {
                FileEditorManager.getInstance(it).openFile(virtualFile, true)
            }
            print("\n", LogConsoleType.EMMY, ConsoleViewContentType.SYSTEM_OUTPUT)
        }

        if (file != null) {
            val script = LoadedScript(file, proto.index, proto.fileName, proto.state)
            loadedScriptMap[proto.index] = script

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
            print("[✘] File not found ", LogConsoleType.EMMY, ConsoleViewContentType.SYSTEM_OUTPUT)
            printHyperlink("[TRY MEMORY FILE]", LogConsoleType.EMMY) {
                bridge.send(DMReqReloadScript(proto.index))
            }
            print(": ${proto.fileName}\n", LogConsoleType.EMMY, ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    private fun onLoadError(proto: DMLoadError) {
        error(proto.message, LogConsoleType.NORMAL)
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
                createEmmyConsole(ui)
            }
        }
    }

    private fun createEmmyConsole(ui: RunnerLayoutUi) {
        val console = TextConsoleBuilderFactory.getInstance().createBuilder(session.project).console
        val name = "Emmy.log"
        val consoleContent = ui.createContent(name, console.component, name, AllIcons.Debugger.Console_log, null)
        consoleContent.isCloseable = false
        consoleContent.isPinnable = false
        consoleContent.displayName = name
        ui.addContent(consoleContent, 2, PlaceInGrid.center, false)
        RunContentBuilder.addAdditionalConsoleEditorActions(console, consoleContent)
        emmyConsole = console
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

    override fun print(text: String, consoleType: LogConsoleType, contentType: ConsoleViewContentType) {
        if (consoleType == LogConsoleType.EMMY)
            emmyConsole?.print(text, contentType)
        else
            super.print(text, consoleType, contentType)
    }

    override fun printHyperlink(text: String, consoleType: LogConsoleType, handler: (project: Project) -> Unit) {
        if (consoleType == LogConsoleType.EMMY)
            emmyConsole?.printHyperlink(text, handler)
        else
            super.printHyperlink(text, consoleType, handler)
    }

    override fun createConsole(): ExecutionConsole {
        val project = session.project
        val state = object : ConsoleState.NotStartedStated() {
            override fun attachTo(viewImpl: ConsoleViewImpl, handler: ProcessHandler?): ConsoleState {
                return this
            }

            override fun isRunning(): Boolean {
                return emmyInputEnabled && !session.isStopped
            }

            override fun sendUserInput(input: String) {
                bridge.send(DMStdin(input.replace("\n", "\r\n")))
            }
        }
        return object : ConsoleViewImpl(project, GlobalSearchScope.allScope(project), false, state, true) {

        }
    }
}