package com.tang.intellij.lua.debugger.commands;

import com.tang.intellij.lua.debugger.LuaDebugProcess;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class DefaultCommand extends DebugCommand {

    private String commandline;
    protected int requireRespLines;
    private int handleLines;

    public DefaultCommand(String commandline) {
        this(commandline, 1);
    }

    public DefaultCommand(String commandline, int requireRespLines) {
        this.commandline = commandline;
        this.requireRespLines = requireRespLines;
    }

    @Override
    public void write(OutputStreamWriter writer) throws IOException {
        writer.write(commandline);
    }

    @Override
    public final boolean handle(String data) {
        data = data.replace("--[[..skipped..]]", "");
        handle(handleLines, data);
        return requireRespLines == ++handleLines;
    }

    @Override
    public int getRequireRespLines() {
        return requireRespLines;
    }

    protected void handle(int index, String data) {

    }

    public void exec() {
        LuaDebugProcess.getCurrent().runCommand(this);
    }
}
