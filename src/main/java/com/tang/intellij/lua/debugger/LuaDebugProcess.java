package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.tang.intellij.lua.debugger.mobdebug.MobServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaDebugProcess extends XDebugProcess {

    private LuaDebuggerEditorsProvider editorsProvider;
    private MobServer mobServer;

    LuaDebugProcess(@NotNull XDebugSession session) {
        super(session);
        editorsProvider = new LuaDebuggerEditorsProvider();
        mobServer = new MobServer();
        try {
            mobServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }

    @Override
    public void stop() {
        if (mobServer != null) {
            mobServer.stop();
        }
    }
}
