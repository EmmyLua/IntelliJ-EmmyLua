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

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.actions.XDebuggerActions

/**
 *
 * Created by tangzx on 2017/5/1.
 */
abstract class LuaDebugProcess protected constructor(session: XDebugSession) : XDebugProcess(session), DebugLogger {

    override fun sessionInitialized() {
        super.sessionInitialized()
        session.consoleView.addMessageFilter(LuaTracebackFilter(session.project))
    }

    override fun registerAdditionalActions(leftToolbar: DefaultActionGroup, topToolbar: DefaultActionGroup, settings: DefaultActionGroup) {
        val actionManager = ActionManager.getInstance()
        topToolbar.remove(actionManager.getAction(XDebuggerActions.RUN_TO_CURSOR))
        topToolbar.remove(actionManager.getAction(XDebuggerActions.FORCE_STEP_INTO))
    }

    override fun print(text: String, consoleType: LogConsoleType, contentType: ConsoleViewContentType) {
        session.consoleView.print(text, contentType)
    }

    override fun println(text: String, consoleType: LogConsoleType, contentType: ConsoleViewContentType) {
        print("$text\n", consoleType, contentType)
    }

    override fun error(text: String, consoleType: LogConsoleType) {
        print("$text\n", consoleType, ConsoleViewContentType.ERROR_OUTPUT)
    }

    override fun printHyperlink(text: String, consoleType: LogConsoleType, handler: (project: Project) -> Unit) {
        session.consoleView.printHyperlink(text, handler)
    }

    override fun resume(context: XSuspendContext?) {
        run()
    }

    override fun startStepOut(context: XSuspendContext?) {
        startStepOver(context)
    }

    override fun startForceStepInto(context: XSuspendContext?) {
        startStepInto(context)
    }

    override fun runToPosition(position: XSourcePosition, context: XSuspendContext?) {
        resume(context)
    }

    override fun getBreakpointHandlers(): Array<XBreakpointHandler<*>> {
        return arrayOf(object : XBreakpointHandler<XLineBreakpoint<XBreakpointProperties<*>>>(LuaLineBreakpointType::class.java) {
            override fun registerBreakpoint(breakpoint: XLineBreakpoint<XBreakpointProperties<*>>) {
                val sourcePosition = breakpoint.sourcePosition
                if (sourcePosition != null) {
                    registerBreakpoint(sourcePosition, breakpoint)
                }
            }

            override fun unregisterBreakpoint(breakpoint: XLineBreakpoint<XBreakpointProperties<*>>, temporary: Boolean) {
                val sourcePosition = breakpoint.sourcePosition
                if (sourcePosition != null) {
                    unregisterBreakpoint(sourcePosition, breakpoint)
                }
            }
        })
    }

    protected fun processBreakpoint(processor: Processor<XLineBreakpoint<*>>) {
        ApplicationManager.getApplication().runReadAction {
            val breakpoints = XDebuggerManager.getInstance(session.project)
                    .breakpointManager
                    .getBreakpoints(LuaLineBreakpointType::class.java)
            ContainerUtil.process(breakpoints, processor)
        }
    }

    protected open fun registerBreakpoint(sourcePosition: XSourcePosition, breakpoint: XLineBreakpoint<*>) {
    }

    protected open fun unregisterBreakpoint(sourcePosition: XSourcePosition, breakpoint: XLineBreakpoint<*>) {
    }

    private fun getBreakpoint(file: VirtualFile, line: Int): XLineBreakpoint<*>? {
        var bp:XLineBreakpoint<*>? = null
        processBreakpoint(Processor {
            val pos = it.sourcePosition
            if (file == pos?.file && line == pos.line) {
                bp = it
            }
            true
        })
        return bp
    }

    fun setStack(stack: LuaExecutionStack) {
        val frames = stack.stackFrames
        for (topFrame in frames) {
            val sourcePosition = topFrame.sourcePosition
            if (sourcePosition != null) {
                stack.setTopFrame(topFrame)
                val breakpoint = getBreakpoint(sourcePosition.file, sourcePosition.line)
                if (breakpoint != null) {
                    ApplicationManager.getApplication().invokeLater {
                        session.breakpointReached(breakpoint, null, LuaSuspendContext(stack))
                        session.showExecutionPoint()
                    }
                } else {
                    ApplicationManager.getApplication().invokeLater {
                        val se = session
                        if (se is XDebugSessionImpl)
                            se.positionReached(LuaSuspendContext(stack), true)
                        else
                            se.positionReached(LuaSuspendContext(stack))
                        session.showExecutionPoint()
                    }
                }
                return
            }
        }

        // file and source position not found, run it
        run()
    }

    protected abstract fun run()
}
