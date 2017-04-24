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
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.tang.intellij.lua.debugger.attach.protos.LuaAttachEvalResultProto;
import com.tang.intellij.lua.debugger.attach.protos.LuaAttachProto;
import com.tang.intellij.lua.debugger.attach.value.LuaXValue;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * debug bridge
 * Created by tangzx on 2017/3/26.
 */
public class LuaAttachBridge {
    private String pid;
    private XDebugSession session;
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Thread writerThread;
    private Thread readerThread;
    private boolean isRunning;
    private ProtoHandler protoHandler;
    private ProtoFactory protoFactory;
    private int evalIdCounter = 0;
    private Map<Integer, EvalInfo> callbackMap = new HashMap<>();

    void setProtoHandler(ProtoHandler protoHandler) {
        this.protoHandler = protoHandler;
    }

    void setProtoFactory(ProtoFactory protoFactory) {
        this.protoFactory = protoFactory;
    }

    public interface ProtoHandler {
        void handle(LuaAttachProto proto);
    }
    public interface ProtoFactory {
        LuaAttachProto createProto(int type);
    }

    public interface EvalCallback {
        void onResult(LuaAttachEvalResultProto result);
    }

    class EvalInfo {
        EvalCallback callback;
        public String expr;
    }

    LuaAttachBridge(ProcessInfo processInfo, XDebugSession session) {
        pid = String.valueOf(processInfo.getPid());
        this.session = session;
    }

    private Runnable readProcess = new Runnable() {
        @Override
        public void run() {
            boolean readProto = false;
            StringBuilder sb = null;
            while (isRunning) {
                try {
                    String line = reader.readLine();
                    if (line == null)
                        break;
                    if (readProto) {
                        if (line.startsWith("[end]")) {
                            readProto = false;
                            String data = sb.toString();
                            LuaAttachProto proto = parse(data);
                            if (proto != null)
                                handleProto(proto);
                        } else {
                            sb.append(line);
                        }
                    } else {
                        readProto = line.startsWith("[start]");
                        if (readProto) {
                            sb = new StringBuilder();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            stop(false);
        }
    };

    private void handleProto(LuaAttachProto proto) {
        if (proto.getType() == LuaAttachProto.EvalResult) {
            handleEvalCallback((LuaAttachEvalResultProto)proto);
        } else if (protoHandler != null)
            protoHandler.handle(proto);
    }

    private void handleEvalCallback(LuaAttachEvalResultProto proto) {
        EvalInfo info = callbackMap.remove(proto.getEvalId());
        if (info != null) {
            LuaXValue xValue = proto.getXValue();
            if (xValue != null)
                xValue.setName(info.expr);
            info.callback.onResult(proto);
        }
    }

    private Runnable writeProcess = () -> {

    };

    public void start() {
        VirtualFile pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory();
        try {
            if (pluginVirtualDirectory != null) {
                // check arch
                String archExe = LuaFileUtil.getPluginVirtualFile("debugger/windows/Arch.exe");
                ProcessBuilder processBuilder = new ProcessBuilder(archExe);
                boolean isX86;
                Process archChecker = processBuilder.command(archExe, String.valueOf(pid)).start();
                archChecker.waitFor();
                int exitValue = archChecker.exitValue();
                isX86 = exitValue == 1;

                String archType = isX86 ? "x86" : "x64";
                session.getConsoleView().print(String.format("try attach to pid:%s with %s debugger.\n", pid, archType), ConsoleViewContentType.SYSTEM_OUTPUT);
                // attach debugger
                String exe = LuaFileUtil.getPluginVirtualFile(String.format("debugger/windows/%s/Debugger.exe", archType));

                processBuilder = new ProcessBuilder(exe);

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
            }
        } catch (Exception e) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(stream);
            e.printStackTrace(ps);
            session.getConsoleView().print(stream.toString(), ConsoleViewContentType.ERROR_OUTPUT);
            session.stop();
            isRunning = false;
        }
    }

    void stop() {
        stop(true);
    }

    private void stop(boolean detach) {
        if (detach)
            send("detach");
        writer = null;
        reader = null;
        isRunning = false;
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

    void send(String data) {
        if (writer != null) {
            try {
                writer.write(data);
                writer.write('\n');
                writer.flush();
            } catch (IOException e) {
                writer = null;
                session.stop();
            }
        }
    }

    private LuaAttachProto parse(String data) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            data = "<data>" + data + "</data>";
            Document document = documentBuilder.parse(new ByteArrayInputStream(data.getBytes()));
            Element root = document.getDocumentElement();
            NodeList childNodes = root.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeName().equals("type")) {
                    int type = Integer.parseInt(item.getTextContent());
                    return createProto(type, root);
                }
            }
        } catch (Exception e) {
            System.out.println("Parse exception:");
            System.out.println(data);
        }
        return null;
    }

    private LuaAttachProto createProto(int type, Element root) throws Exception {
        LuaAttachProto proto = protoFactory.createProto(type);
        proto.doParse(root);
        return proto;
    }

    public void eval(String expr, int stack, int depth, @NotNull EvalCallback callback) {
        int id = evalIdCounter++;
        EvalInfo info = new  EvalInfo();
        info.callback = callback;
        info.expr = expr;
        callbackMap.put(id, info);
        send(String.format("eval %d %d %d %s", id, stack, depth, expr));
    }

    void sendToggleBreakpoint(int idx, int line) {
        send(String.format("setb %d %d", idx, line));
    }

    void sendDone() {
        send("done");
    }

    void sendRun() {
        send("run");
    }
}
