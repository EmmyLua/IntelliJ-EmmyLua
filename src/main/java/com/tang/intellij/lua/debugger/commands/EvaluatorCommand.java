package com.tang.intellij.lua.debugger.commands;

import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.tang.intellij.lua.debugger.LuaNamedValue;

/**
 *
 * Created by tangzx on 2017/1/1.
 */
public class EvaluatorCommand extends DefaultCommand {

    private final XDebuggerEvaluator.XEvaluationCallback xEvaluationCallback;

    public EvaluatorCommand(String expr, XDebuggerEvaluator.XEvaluationCallback xEvaluationCallback) {
        super("EXEC return " + expr, 2);
        this.xEvaluationCallback = xEvaluationCallback;
    }

    @Override
    protected void handle(int index, String data) {
        super.handle(index, data);
        if (index == 1) {
            xEvaluationCallback.evaluated(new LuaNamedValue("kkk", data));
        }
    }
}
