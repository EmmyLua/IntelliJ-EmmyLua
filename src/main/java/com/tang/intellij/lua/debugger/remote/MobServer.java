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

package com.tang.intellij.lua.debugger.remote;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.io.BaseOutputReader;
import com.tang.intellij.lua.debugger.remote.commands.DebugCommand;
import com.tang.intellij.lua.debugger.remote.commands.DefaultCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class MobServer implements Runnable {

    class LuaDebugReader extends BaseOutputReader {
        LuaDebugReader(@NotNull InputStream inputStream, @Nullable Charset charset) {
            super(inputStream, charset);
            start(getClass().getName());
        }

        @Override
        protected void onTextAvailable(@NotNull String s) {
            MobServer.this.onResp(s);
        }

        @NotNull
        @Override
        protected Future<?> executeOnPooledThread(@NotNull Runnable runnable) {
            return ApplicationManager.getApplication().executeOnPooledThread(runnable);
        }
    }

    private ServerSocket server;
    private Thread thread;
    private Future threadSend;
    private LuaMobDebugProcess listener;
    private Queue<DebugCommand> commands = new LinkedList<>();
    private LuaDebugReader debugReader;
    private DebugCommand currentCommandWaitForResp;
    private OutputStreamWriter streamWriter;
    private StringBuffer stringBuffer = new StringBuffer(2048);

    public MobServer(LuaMobDebugProcess listener) {
        this.listener = listener;
    }

    public void start(int port) throws IOException {
        if (server == null)
            server = new ServerSocket(port);
        thread = new Thread(this);
        thread.start();
    }

    private void onResp(String data) {
        System.out.println(data);
        if (currentCommandWaitForResp != null) {
            stringBuffer.append(data);
            int eat = currentCommandWaitForResp.handle(stringBuffer.toString());
            if (eat > 0) {
                stringBuffer.delete(0, eat);
                if (currentCommandWaitForResp.isFinished())
                    currentCommandWaitForResp = null;
            }
        } else {
            stringBuffer.setLength(0);
        }

        Pattern pattern = Pattern.compile("(\\d+) (\\w+)( (.+))?");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            int code = Integer.parseInt(matcher.group(1));
            //String status = matcher.group(2);
            String context = matcher.group(4);
            listener.handleResp(code, context);
        }
    }

    public void write(String data) throws IOException {
        streamWriter.write(data);
        System.out.println("send:" + data);
    }

    @Override
    public void run() {
        try {
            final Socket accept = server.accept();
            listener.println("Connected.");
            debugReader = new LuaDebugReader(accept.getInputStream(), Charset.forName("UTF-8"));

            threadSend = ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    streamWriter = new OutputStreamWriter(accept.getOutputStream(), Charset.forName("UTF-8"));
                    boolean firstTime = true;

                    while (accept.isConnected()) {
                        DebugCommand command;
                        while (commands.size() > 0 && currentCommandWaitForResp == null) {
                            if (currentCommandWaitForResp == null) {
                                command = commands.poll();
                                command.setDebugProcess(listener);
                                command.write(this);
                                streamWriter.write("\n");
                                streamWriter.flush();
                                if (command.getRequireRespLines() > 0)
                                    currentCommandWaitForResp = command;
                            }
                        }
                        if (firstTime) {
                            firstTime = false;
                            addCommand("RUN");
                        }
                        Thread.sleep(5);
                    }

                    listener.println("Disconnected.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        currentCommandWaitForResp = null;
        if (thread != null)
            thread.interrupt();
        if (threadSend != null)
            threadSend.cancel(true);
        if (debugReader != null)
            debugReader.stop();
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCommand(String command) {
        addCommand(new DefaultCommand(command, 0));
    }

    public void addCommand(DebugCommand command) {
        commands.add(command);
    }
}
