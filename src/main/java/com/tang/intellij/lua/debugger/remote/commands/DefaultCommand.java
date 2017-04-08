/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.debugger.remote.commands;

import com.tang.intellij.lua.debugger.remote.LuaDebugProcess;

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
