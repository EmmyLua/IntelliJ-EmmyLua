package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaExecutionStack extends XExecutionStack {
    private LuaStackFrame frame = new LuaStackFrame();

    LuaExecutionStack() {
        super("LuaStack");
    }

    @Nullable
    @Override
    public XStackFrame getTopFrame() {
        return frame;
    }

    @Override
    public void computeStackFrames(int i, XStackFrameContainer xStackFrameContainer) {

    }
}
