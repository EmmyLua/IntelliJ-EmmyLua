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

package com.tang.intellij.lua.debugger.attach;

import com.intellij.execution.process.ProcessInfo;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider;
import com.tang.intellij.lua.debugger.LuaLineBreakpointType;
import com.tang.intellij.lua.debugger.LuaSuspendContext;
import com.tang.intellij.lua.debugger.attach.protos.*;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Created by tangzx on 2017/3/26.
 */
public class LuaAttachDebugProcess extends XDebugProcess implements LuaAttachBridge.ProtoHandler, LuaAttachBridge.ProtoFactory {
    private LuaDebuggerEditorsProvider editorsProvider;
    private LuaAttachBridge bridge;
    private Map<XSourcePosition, XLineBreakpoint> registeredBreakpoints = new ConcurrentHashMap<>();
    private Map<Integer, LoadedScript> loadedScriptMap = new ConcurrentHashMap<>();

    LuaAttachDebugProcess(@NotNull XDebugSession session, ProcessInfo processInfo) {
        super(session);
        editorsProvider = new LuaDebuggerEditorsProvider();
        bridge = new LuaAttachBridge(processInfo, session);
        bridge.setProtoHandler(this);
        bridge.setProtoFactory(this);
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
        bridge.start();
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        bridge.send("stepover");
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        bridge.send("stepinto");
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {

    }

    @Override
    public void stop() {
        bridge.stop();
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        bridge.sendRun();
    }

    @Override
    public void handle(LuaAttachProto proto) {
        int type = proto.getType();
        switch (type) {
            case LuaAttachProto.Message:
                LuaAttachMessageProto messageProto = (LuaAttachMessageProto) proto;
                messageProto.outputToConsole();
                break;
            case LuaAttachProto.LoadScript:
                LuaAttachLoadScriptProto loadScriptProto = (LuaAttachLoadScriptProto) proto;
                onLoadScript(loadScriptProto);
                break;
            case LuaAttachProto.Break:
                onBreak((LuaAttachBreakProto) proto);
                break;
            case LuaAttachProto.SessionEnd:
            case LuaAttachProto.DestroyVM:
                // todo : 发送detach命令Server端会挂
                bridge.stop(false);
                getSession().stop();
                break;
        }
    }

    private void onBreak(LuaAttachBreakProto proto) {
        VirtualFile file = LuaFileUtil.findFile(getSession().getProject(), proto.getName());
        if (file == null) {
            bridge.sendRun();
            return;
        }

        for (XSourcePosition pos : registeredBreakpoints.keySet()) {
            if (file.equals(pos.getFile()) && proto.getLine() == pos.getLine()) {
                final XLineBreakpoint breakpoint = registeredBreakpoints.get(pos);
                ApplicationManager.getApplication().invokeLater(()-> {
                    getSession().breakpointReached(breakpoint, null, new LuaSuspendContext(proto.getStack()));
                    getSession().showExecutionPoint();
                });
                return;
            }
        }

        //position reached
        ApplicationManager.getApplication().invokeLater(()-> {
            getSession().positionReached(new LuaSuspendContext(proto.getStack()));
            getSession().showExecutionPoint();
        });
    }

    private void onLoadScript(LuaAttachLoadScriptProto proto) {
        VirtualFile file = LuaFileUtil.findFile(getSession().getProject(), proto.getName());
        if (file == null) {
            getSession().getConsoleView().print(String.format("File not found : %s\n", proto.getName()), ConsoleViewContentType.SYSTEM_OUTPUT);
        } else {
            LoadedScript script = new LoadedScript(file, proto.getIndex(), proto.getName());
            loadedScriptMap.put(proto.getIndex(), script);

            for (XSourcePosition pos : registeredBreakpoints.keySet()) {
                if (file.equals(pos.getFile())) {
                    bridge.sendToggleBreakpoint(proto.getIndex(), pos.getLine());
                }
            }
        }
        bridge.sendDone();
    }

    @Nullable
    public LoadedScript getScript(int index) {
        return loadedScriptMap.get(index);
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return new XBreakpointHandler[] { new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(LuaLineBreakpointType.class) {
            @Override
            public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint) {
                XSourcePosition sourcePosition = breakpoint.getSourcePosition();
                if (sourcePosition != null) {
                    registeredBreakpoints.put(sourcePosition, breakpoint);
                    for (LoadedScript script : loadedScriptMap.values()) {
                        if (script.getFile().equals(sourcePosition.getFile())) {
                            bridge.sendToggleBreakpoint(script.getIndex(), sourcePosition.getLine());
                            break;
                        }
                    }
                }
            }

            @Override
            public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, boolean temporary) {
                XSourcePosition sourcePosition = breakpoint.getSourcePosition();
                if (sourcePosition != null) {
                    registeredBreakpoints.remove(sourcePosition);
                    for (LoadedScript script : loadedScriptMap.values()) {
                        if (script.getFile().equals(sourcePosition.getFile())) {
                            bridge.sendToggleBreakpoint(script.getIndex(), sourcePosition.getLine());
                            break;
                        }
                    }
                }
            }
        } };
    }

    @Override
    public LuaAttachProto createProto(int type) {
        LuaAttachProto proto;
        switch (type) {
            case LuaAttachProto.Message:
                proto = new LuaAttachMessageProto();
                break;
            case LuaAttachProto.LoadScript:
                proto = new LuaAttachLoadScriptProto();
                break;
            case LuaAttachProto.SetBreakpoint:
                proto = new LuaAttachSetBreakpointProto();
                break;
            case LuaAttachProto.Break:
                proto = new LuaAttachBreakProto();
                break;
            case LuaAttachProto.EvalResult:
                proto = new LuaAttachEvalResultProto();
                break;
            default:
                proto = new LuaAttachProto(type);
        }
        proto.setProcess(this);
        return proto;
    }

    public LuaAttachBridge getBridge() {
        return bridge;
    }
}
