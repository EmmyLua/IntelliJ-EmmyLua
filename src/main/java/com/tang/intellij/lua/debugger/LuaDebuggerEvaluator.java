package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.tang.intellij.lua.debugger.commands.EvaluatorCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaDebuggerEvaluator extends XDebuggerEvaluator {
    @Override
    public void evaluate(@NotNull String s, @NotNull XEvaluationCallback xEvaluationCallback, @Nullable XSourcePosition xSourcePosition) {
        EvaluatorCommand evaluatorCommand = new EvaluatorCommand("return " + s, false, data -> {
            xEvaluationCallback.evaluated(LuaNamedValue.createEvalValue(s, data));
        });
        evaluatorCommand.exec();
    }
}
