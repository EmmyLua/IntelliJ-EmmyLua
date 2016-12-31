package com.tang.intellij.lua.debugger.commands;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class DefaultCommand extends DebugCommand {

    private String commandline;

    public DefaultCommand(String commandline) {

        this.commandline = commandline;
    }

    @Override
    public void write(OutputStreamWriter writer) throws IOException {
        writer.write(commandline);
    }

    @Override
    public void handle(String data) {

    }
}
