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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.impl.actions.XDebuggerActions
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * Created by tangzx on 2017/5/1.
 */
abstract class LuaDebugProcess protected constructor(session: XDebugSession) : XDebugProcess(session), DebugLogger {
    protected var registeredBreakpoints: MutableMap<XSourcePosition, XLineBreakpoint<*>> = ConcurrentHashMap()

    override fun sessionInitialized() {
        super.sessionInitialized()
        session.consoleView.addMessageFilter(LuaTracebackFilter(session.project))
    }

    override fun registerAdditionalActions(leftToolbar: DefaultActionGroup, topToolbar: DefaultActionGroup, settings: DefaultActionGroup) {
        val actionManager = ActionManager.getInstance()
        topToolbar.remove(actionManager.getAction(XDebuggerActions.RUN_TO_CURSOR))
        topToolbar.remove(actionManager.getAction(XDebuggerActions.FORCE_STEP_INTO))
    }

    override fun print(text: String, type: ConsoleViewContentType) {
        session.consoleView.print(text, type)
    }

    override fun println(text: String, type: ConsoleViewContentType) {
        session.consoleView.print(text + "\n", type)
    }

    override fun error(text: String) {
        session.consoleView.print(text + "\n", ConsoleViewContentType.ERROR_OUTPUT)
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
                    this@LuaDebugProcess.registerBreakpoint(sourcePosition, breakpoint)
                }
            }

            override fun unregisterBreakpoint(breakpoint: XLineBreakpoint<XBreakpointProperties<*>>, temporary: Boolean) {
                val sourcePosition = breakpoint.sourcePosition
                if (sourcePosition != null) {
                    this@LuaDebugProcess.unregisterBreakpoint(sourcePosition, breakpoint)
                }
            }
        })
    }

    protected open fun registerBreakpoint(sourcePosition: XSourcePosition, breakpoint: XLineBreakpoint<*>) {
        registeredBreakpoints.put(sourcePosition, breakpoint)
    }

    protected open fun unregisterBreakpoint(sourcePosition: XSourcePosition, breakpoint: XLineBreakpoint<*>) {
        registeredBreakpoints.remove(sourcePosition)
    }

    private fun getBreakpoint(file: VirtualFile, line: Int): XLineBreakpoint<*>? {
        for (pos in registeredBreakpoints.keys) {
            if (file == pos.file && line == pos.line) {
                return registeredBreakpoints[pos]
            }
        }
        return null
    }

    fun setStack(stack: LuaExecutionStack) {
        val frames = stack.stackFrames
        for (topFrame in frames) {
            val sourcePosition = topFrame.sourcePosition
            if (sourcePosition != null) {
                stack.topFrame = topFrame
                val breakpoint = getBreakpoint(sourcePosition.file, sourcePosition.line)
                if (breakpoint != null) {
                    ApplicationManager.getApplication().invokeLater {
                        session.breakpointReached(breakpoint, null, LuaSuspendContext(stack))
                        session.showExecutionPoint()
                    }
                } else {
                    ApplicationManager.getApplication().invokeLater {
                        session.positionReached(LuaSuspendContext(stack))
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
