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

package com.tang.intellij.lua.debugger.app;

import com.intellij.xdebugger.XDebugSession;
import com.tang.intellij.lua.debugger.attach.LuaAttachBridge;
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcess;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/5/7.
 */
public class LuaAppAttachProcess extends LuaAttachDebugProcess {

    LuaAppAttachProcess(@NotNull XDebugSession session) {
        super(session);
    }

    @Override
    protected LuaAttachBridge startBridge() {
        LuaAppRunConfiguration configuration = (LuaAppRunConfiguration) getSession().getRunProfile();
        assert configuration != null;
        String workingDir = configuration.getWorkingDir();

        bridge = new LuaAttachBridge(getSession());
        bridge.setProtoHandler(this);
        bridge.setProtoFactory(this);
        bridge.launch(configuration.getProgram(), workingDir, new String[] { configuration.getFile() });

        return bridge;
    }
}
