package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaExecutionStack extends XExecutionStack {
    private LuaStackFrame frame;
    private List<LuaStackFrame> stackFrameList;

    public LuaExecutionStack(List<LuaStackFrame> stackFrameList) {
        super("LuaStack");
        this.stackFrameList = stackFrameList;
        if (!stackFrameList.isEmpty())
            frame = stackFrameList.get(0);
    }

    @Nullable
    @Override
    public XStackFrame getTopFrame() {
        return frame;
    }

    @Override
    public void computeStackFrames(int i, XStackFrameContainer xStackFrameContainer) {
        xStackFrameContainer.addStackFrames(stackFrameList, true);
    }
}
