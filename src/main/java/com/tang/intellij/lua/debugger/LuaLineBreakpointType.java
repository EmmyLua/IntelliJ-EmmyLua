package com.tang.intellij.lua.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/12/30.
 */
public class LuaLineBreakpointType extends XLineBreakpointTypeBase {

    private static final String ID = "lua-line";
    private static final String NAME = "lua-line-breakpoint";

    protected LuaLineBreakpointType() {
        super(ID, NAME, new LuaDebuggerEditorsProvider());
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        return true;
    }
}
