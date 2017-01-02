package com.tang.intellij.lua.debugger.commands;

import com.tang.intellij.lua.debugger.LuaDebugProcess;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Remote Debug Command
 * Created by tangzx on 2016/12/31.
 */
public abstract class DebugCommand {

    protected LuaDebugProcess debugProcess;

    public void setDebugProcess(LuaDebugProcess process) {
        debugProcess = process;
    }

    public abstract void write(OutputStreamWriter writer) throws IOException;
    public abstract boolean handle(String data);
    public abstract int getRequireRespLines();
}
