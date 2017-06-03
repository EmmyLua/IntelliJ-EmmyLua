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

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.tang.intellij.lua.debugger.LuaDebugProcess;
import com.tang.intellij.lua.debugger.LuaDebuggerEditorsProvider;
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
public abstract class LuaAttachDebugProcess extends LuaDebugProcess implements LuaAttachBridge.ProtoHandler, LuaAttachBridge.ProtoFactory {
    private LuaDebuggerEditorsProvider editorsProvider;
    protected LuaAttachBridge bridge;
    private Map<Integer, LoadedScript> loadedScriptMap = new ConcurrentHashMap<>();

    protected LuaAttachDebugProcess(@NotNull XDebugSession session) {
        super(session);
        session.setPauseActionSupported(false);
        editorsProvider = new LuaDebuggerEditorsProvider();
    }

    protected abstract LuaAttachBridge startBridge();

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
        bridge = startBridge();
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
    public void startStepOut(@Nullable XSuspendContext context) {
        bridge.send("stepout");
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
            case LuaAttachProto.Exception:
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

        XLineBreakpoint breakpoint = getBreakpoint(file, proto.getLine());
        if (breakpoint != null) {
            ApplicationManager.getApplication().invokeLater(()-> {
                getSession().breakpointReached(breakpoint, null, new LuaSuspendContext(proto.getStack()));
                getSession().showExecutionPoint();
            });
        } else {
            //position reached
            ApplicationManager.getApplication().invokeLater(() -> {
                getSession().positionReached(new LuaSuspendContext(proto.getStack()));
                getSession().showExecutionPoint();
            });
        }
    }

    private void onLoadScript(LuaAttachLoadScriptProto proto) {
        VirtualFile file = LuaFileUtil.findFile(getSession().getProject(), proto.getName());
        if (file == null) {
            getSession().getConsoleView().print(String.format("[✘] File not found : %s\n", proto.getName()), ConsoleViewContentType.SYSTEM_OUTPUT);
        } else {
            LoadedScript script = new LoadedScript(file, proto.getIndex(), proto.getName());
            loadedScriptMap.put(proto.getIndex(), script);
            getSession().getConsoleView().print(String.format("[✔] File was loaded : %s\n", proto.getName()), ConsoleViewContentType.SYSTEM_OUTPUT);

            for (XSourcePosition pos : registeredBreakpoints.keySet()) {
                if (LuaFileUtil.fileEquals(file, pos.getFile())) {
                    XLineBreakpoint breakpoint = registeredBreakpoints.get(pos);
                    bridge.addBreakpoint(proto.getIndex(), breakpoint);
                }
            }
        }
        bridge.sendDone();
    }

    @Override
    protected void registerBreakpoint(@NotNull XSourcePosition sourcePosition, @NotNull XLineBreakpoint breakpoint) {
        super.registerBreakpoint(sourcePosition, breakpoint);
        for (LoadedScript script : loadedScriptMap.values()) {
            if (LuaFileUtil.fileEquals(sourcePosition.getFile(), script.getFile())) {
                bridge.addBreakpoint(script.getIndex(), breakpoint);
                break;
            }
        }
    }

    @Override
    protected void unregisterBreakpoint(@NotNull XSourcePosition sourcePosition, @NotNull XLineBreakpoint breakpoint) {
        super.unregisterBreakpoint(sourcePosition, breakpoint);
        VirtualFile sourceFile = sourcePosition.getFile();

        for (LoadedScript script : loadedScriptMap.values()) {
            VirtualFile scriptFile = script.getFile();
            if (LuaFileUtil.fileEquals(sourceFile, scriptFile)) {
                bridge.removeBreakpoint(script.getIndex(), breakpoint);
                break;
            }
        }
    }

    @Nullable
    public LoadedScript getScript(int index) {
        return loadedScriptMap.get(index);
    }

    @Override
    public LuaAttachProto createProto(int type) {
        LuaAttachProto proto;
        switch (type) {
            case LuaAttachProto.Exception:
            case LuaAttachProto.Message:
                proto = new LuaAttachMessageProto(type);
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
