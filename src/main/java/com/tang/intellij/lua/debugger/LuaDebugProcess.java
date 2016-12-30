package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaDebugProcess extends XDebugProcess {

    private LuaDebuggerEditorsProvider editorsProvider;

    LuaDebugProcess(@NotNull XDebugSession session) {
        super(session);
        editorsProvider = new LuaDebuggerEditorsProvider();
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }
}
