package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaStackFrame extends XStackFrame {

    private LuaDebuggerEvaluator evaluator = new LuaDebuggerEvaluator();
    private XSourcePosition position;

    public LuaStackFrame(XSourcePosition position) {
        this.position = position;
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        return evaluator;
    }

    @Nullable
    @Override
    public XSourcePosition getSourcePosition() {
        return position;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        node.setErrorMessage("err");
        super.computeChildren(node);
    }
}
