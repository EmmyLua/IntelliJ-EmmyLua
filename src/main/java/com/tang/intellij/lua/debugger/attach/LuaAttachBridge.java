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

import java.io.*;

/**
 * debug bridge
 * Created by tangzx on 2017/3/26.
 */
class LuaAttachBridge {
    private String pid;
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Thread writerThread;
    private Thread readerThread;
    private boolean isRunning;

    LuaAttachBridge(ProcessInfo processInfo) {
        pid = String.valueOf(processInfo.getPid());
    }

    private Runnable readProcess = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    String line = reader.readLine();
                    if (line == null || "".equals(line))
                        continue;

                    int size = Integer.parseInt(line);
                    char[] buff = new char[size];
                    int read = reader.read(buff, 0, size);
                    assert read == size;
                    String s = String.copyValueOf(buff);
                    System.out.println(s);
                } catch (IOException e) {
                    e.printStackTrace();
                    stop();
                    break;
                }
            }
        }
    };

    private Runnable writeProcess = () -> {

    };

    public void start() {
        VirtualFile pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory();
        if (pluginVirtualDirectory != null) {
            String exe = pluginVirtualDirectory.getPath() + "/classes/debugger/windows/x64/Debugger.exe";
            ProcessBuilder processBuilder = new ProcessBuilder(exe);
            try {
                process = processBuilder.start();
                writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                writer.write(pid);
                writer.write('\n');
                writer.flush();

                readerThread = new Thread(readProcess);
                readerThread.start();
                writerThread = new Thread(writeProcess);
                writerThread.start();
                isRunning = true;
            } catch (IOException e) {
                e.printStackTrace();
                isRunning = false;
            }
        }
    }

    public void stop() {
        if (process != null) {
            process.destroy();
            process = null;
            isRunning = false;
        }
        if (writerThread != null) {
            writerThread.interrupt();
        }
        if (readerThread != null) {
            readerThread.interrupt();
        }
    }
}
