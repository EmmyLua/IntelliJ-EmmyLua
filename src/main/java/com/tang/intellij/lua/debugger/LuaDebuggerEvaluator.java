package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaDebuggerEvaluator extends XDebuggerEvaluator {
    @Override
    public void evaluate(@NotNull String s, @NotNull XEvaluationCallback xEvaluationCallback, @Nullable XSourcePosition xSourcePosition) {
        //xEvaluationCallback.evaluated();
        System.out.println("eval : " + s);
    }
}
