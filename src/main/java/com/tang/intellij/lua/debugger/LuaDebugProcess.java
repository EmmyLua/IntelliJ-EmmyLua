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

package com.tang.intellij.lua.debugger;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Created by tangzx on 2017/5/1.
 */
public abstract class LuaDebugProcess extends XDebugProcess implements DebugLogger {
    protected Map<XSourcePosition, XLineBreakpoint> registeredBreakpoints = new ConcurrentHashMap<>();

    protected LuaDebugProcess(@NotNull XDebugSession session) {
        super(session);
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
        getSession().getConsoleView().addMessageFilter(new LuaTracebackFilter(getSession().getProject()));
    }

    @Override
    public void print(@NotNull String text) {
        getSession().getConsoleView().print(text, ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    public void println(@NotNull String text) {
        getSession().getConsoleView().print(text + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    public void error(@NotNull String text) {
        getSession().getConsoleView().print(text + "\n", ConsoleViewContentType.ERROR_OUTPUT);
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        startStepOver(context);
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {
        startStepInto(context);
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        resume(context);
    }

    @NotNull
    @Override
    public final XBreakpointHandler<?>[] getBreakpointHandlers() {
        return new XBreakpointHandler[] { new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(LuaLineBreakpointType.class) {
            @Override
            public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
                XSourcePosition sourcePosition = breakpoint.getSourcePosition();
                if (sourcePosition != null) {
                    LuaDebugProcess.this.registerBreakpoint(sourcePosition, breakpoint);
                }
            }

            @Override
            public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, boolean temporary) {
                XSourcePosition sourcePosition = breakpoint.getSourcePosition();
                if (sourcePosition != null) {
                    LuaDebugProcess.this.unregisterBreakpoint(sourcePosition, breakpoint);
                }
            }
        } };
    }

    protected void registerBreakpoint(@NotNull XSourcePosition sourcePosition, @NotNull XLineBreakpoint breakpoint) {
        registeredBreakpoints.put(sourcePosition, breakpoint);
    }

    protected void unregisterBreakpoint(@NotNull XSourcePosition sourcePosition, @NotNull XLineBreakpoint position) {
        registeredBreakpoints.remove(sourcePosition);
    }

    @Nullable
    protected XLineBreakpoint getBreakpoint(VirtualFile file, int line) {
        for (XSourcePosition pos : registeredBreakpoints.keySet()) {
            if (file.equals(pos.getFile()) && line == pos.getLine()) {
                return registeredBreakpoints.get(pos);
            }
        }
        return null;
    }
}
