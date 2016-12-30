package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/12/30.
 */
public class LuaLineBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {
    private LuaDebugProcess process;

    LuaLineBreakpointHandler(LuaDebugProcess process) {
        super(LuaLineBreakpointType.class);
        this.process = process;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
        process.addBreakpoint(xBreakpoint);
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<XBreakpointProperties> xBreakpoint, boolean b1) {
        process.removeBreakpoint(xBreakpoint);
    }
}
