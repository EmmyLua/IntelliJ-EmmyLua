package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaSuspendContext extends XSuspendContext {

    private LuaExecutionStack active;

    @Nullable
    @Override
    public XExecutionStack getActiveExecutionStack() {
        if (active == null)
            active = new LuaExecutionStack();
        return active;
    }
}
