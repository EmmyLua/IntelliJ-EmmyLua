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

import com.tang.intellij.lua.debugger.remote.LuaMobDebugProcess;
import com.tang.intellij.lua.debugger.remote.MobServer;

import java.io.IOException;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class DefaultCommand extends DebugCommand {

    private String commandline;
    private int requireRespLines;
    int handleLines;

    public DefaultCommand(String commandline) {
        this(commandline, 1);
    }

    public DefaultCommand(String commandline, int requireRespLines) {
        this.commandline = commandline;
        this.requireRespLines = requireRespLines;
    }

    @Override
    public void write(MobServer writer) throws IOException {
        writer.write(commandline);
    }

    @Override
    public int handle(String data) {
        int LB = data.indexOf('\n');
        if (LB == -1) return LB;

        handle(handleLines++, data);
        return data.length();
    }

    @Override
    public boolean isFinished() {
        return requireRespLines <= handleLines;
    }

    @Override
    public int getRequireRespLines() {
        return requireRespLines;
    }

    protected void handle(int index, String data) {

    }

    public void exec() {
        LuaMobDebugProcess.getCurrent().runCommand(this);
    }
}
