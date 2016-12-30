package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.tang.intellij.lua.debugger.mobdebug.MobServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaDebugProcess extends XDebugProcess implements MobServer.Listener {

    private LuaDebuggerEditorsProvider editorsProvider;
    private MobServer mobServer;
    private XLineBreakpoint<XBreakpointProperties> breakpoint;

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

    @Override
    public void resume(@Nullable XSuspendContext context) {
        mobServer.addCommand("RUN");
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        mobServer.addCommand("OVER");
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        mobServer.addCommand("STEP");
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        mobServer.addCommand("OUT");
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return new XBreakpointHandler[] { new LuaLineBreakpointHandler(this) };
    }

    void addBreakpoint(XLineBreakpoint<XBreakpointProperties> breakpoint) {
        this.breakpoint = breakpoint;
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition != null) {
            String shortFilePath = breakpoint.getShortFilePath();
            mobServer.addCommand(String.format("SETB %s %d", shortFilePath, sourcePosition.getLine()));
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
        switch (code) {
            case 202:
                getSession().breakpointReached(breakpoint, "test", new LuaSuspendContext());
                break;
        }
    }
}
