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

package com.tang.intellij.lua.debugger.attach;

import com.intellij.execution.process.ProcessInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.tang.intellij.lua.psi.LuaFileUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * debug bridge
 * Created by tangzx on 2017/3/26.
 */
class LuaAttachBridge {
    private String pid;
    private Process process;

    LuaAttachBridge(ProcessInfo processInfo) {
        pid = String.valueOf(processInfo.getPid());
    }

    public void start() {
        VirtualFile pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory();
        if (pluginVirtualDirectory != null) {
            String exe = pluginVirtualDirectory.getPath() + "/classes/debugger/windows/x64/Debugger.exe";
            ProcessBuilder processBuilder = new ProcessBuilder(exe);
            try {
                process = processBuilder.start();
                OutputStream outputStream = process.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write(pid);
                writer.write('\n');
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void stop() {
        if (process != null) {
            process.destroy();
        }
    }
}
