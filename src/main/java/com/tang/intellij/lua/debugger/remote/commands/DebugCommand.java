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

import com.tang.intellij.lua.debugger.remote.LuaRemoteDebugProcess;
import com.tang.intellij.lua.debugger.remote.mobdebug.MobServer;

import java.io.IOException;

/**
 * Remote Debug Command
 * Created by tangzx on 2016/12/31.
 */
public abstract class DebugCommand {

    protected LuaRemoteDebugProcess debugProcess;

    public void setDebugProcess(LuaRemoteDebugProcess process) {
        debugProcess = process;
    }

    public abstract void write(MobServer writer) throws IOException;
    public abstract boolean handle(String data);
    public abstract int getRequireRespLines();
}
