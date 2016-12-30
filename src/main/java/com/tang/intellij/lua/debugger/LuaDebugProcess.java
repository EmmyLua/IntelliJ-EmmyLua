package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.tang.intellij.lua.debugger.mobdebug.MobServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaDebugProcess extends XDebugProcess implements MobServer.Listener {

    private LuaDebuggerEditorsProvider editorsProvider;
    private MobServer mobServer;

    LuaDebugProcess(@NotNull XDebugSession session) {
        super(session);
        editorsProvider = new LuaDebuggerEditorsProvider();
        mobServer = new MobServer(this);
        try {
            mobServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }

    @Override
    public void stop() {
        if (mobServer != null) {
            mobServer.stop();
        }
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return new XBreakpointHandler[] { new LuaLineBreakpointHandler(this) };
    }

    void addBreakpoint(XLineBreakpoint<XBreakpointProperties> breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition != null) {
            String shortFilePath = breakpoint.getShortFilePath();
            mobServer.addBreakpoint(shortFilePath, sourcePosition.getLine());
        }
    }

    void removeBreakpoint(XLineBreakpoint<XBreakpointProperties> breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition != null) {
            String shortFilePath = breakpoint.getShortFilePath();
            mobServer.addCommand(String.format("DELB %s %d", shortFilePath, sourcePosition.getLine()));
        }
    }

    @Override
    public void handleResp(int code, String[] params) {

    }
}
