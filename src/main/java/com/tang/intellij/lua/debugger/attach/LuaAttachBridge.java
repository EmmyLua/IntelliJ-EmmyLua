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

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
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
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * debug bridge
 * Created by tangzx on 2017/3/26.
 */
public class LuaAttachBridge {
    private OSProcessHandler handler;
    private XDebugSession session;
    private BufferedWriter writer;
    private ProtoHandler protoHandler;
    private ProtoFactory protoFactory;
    private int evalIdCounter = 0;
    private Map<Integer, EvalInfo> callbackMap = new HashMap<>();

    public void setProtoHandler(ProtoHandler protoHandler) {
        this.protoHandler = protoHandler;
    }

    public void setProtoFactory(ProtoFactory protoFactory) {
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

    public LuaAttachBridge(XDebugSession session) {
        this.session = session;
    }

    private ProcessListener processListener = new ProcessListener() {
        private boolean readProto = false;
        private StringBuilder sb;

        @Override
        public void startNotified(ProcessEvent processEvent) {

        }

        @Override
        public void processTerminated(ProcessEvent processEvent) {
            stop(false);
        }

        @Override
        public void processWillTerminate(ProcessEvent processEvent, boolean b) {

        }

        @Override
        public void onTextAvailable(ProcessEvent processEvent, Key key) {
            if (key == ProcessOutputTypes.STDOUT) {
                String line = processEvent.getText();
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
            }
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

    private String getEmmyLua() {
        return LuaFileUtil.getPluginVirtualFile("debugger/Emmy.lua");
    }

    public void attach(int processId) {
        String pid = String.valueOf(processId);
        VirtualFile pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory();
        try {
            if (pluginVirtualDirectory != null) {
                // check arch
                String archExe = LuaFileUtil.getPluginVirtualFile("debugger/windows/Arch.exe");
                ProcessBuilder processBuilder = new ProcessBuilder(archExe);
                boolean isX86;
                Process archChecker = processBuilder.command(archExe, "-pid", pid).start();
                archChecker.waitFor();
                int exitValue = archChecker.exitValue();
                isX86 = exitValue == 1;

                String archType = isX86 ? "x86" : "x64";
                session.getConsoleView().print(String.format("Try attach to pid:%s with %s debugger.\n", pid, archType), ConsoleViewContentType.SYSTEM_OUTPUT);
                // attach debugger
                String exe = LuaFileUtil.getPluginVirtualFile(String.format("debugger/windows/%s/Debugger.exe", archType));

                GeneralCommandLine commandLine = new GeneralCommandLine(exe);
                commandLine.addParameters("-m", "attach", "-p", pid, "-e", getEmmyLua());
                commandLine.setCharset( Charset.forName("UTF-8"));
                handler = new OSProcessHandler(commandLine);
                handler.addProcessListener(processListener);
                handler.startNotify();
                writer = new BufferedWriter(new OutputStreamWriter(handler.getProcess().getOutputStream()));
            }
        } catch (Exception e) {
            session.getConsoleView().print(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
            session.stop();
        }
    }

    public void launch(@NotNull String program, String workingDir, String[] args) {
        VirtualFile pluginVirtualDirectory = LuaFileUtil.getPluginVirtualDirectory();
        try {
            if (pluginVirtualDirectory != null) {
                if (workingDir == null || workingDir.isEmpty()) {
                    throw new Exception("Working directory not found.");
                }

                // check arch
                String archExe = LuaFileUtil.getPluginVirtualFile("debugger/windows/Arch.exe");
                ProcessBuilder processBuilder = new ProcessBuilder(archExe);
                boolean isX86;
                Process archChecker = processBuilder.command(archExe, "-file", program).start();
                archChecker.waitFor();
                int exitValue = archChecker.exitValue();
                if (exitValue == -1) {
                    throw new Exception(String.format("Program [%s] not found.", program));
                }
                isX86 = exitValue == 1;

                String archType = isX86 ? "x86" : "x64";
                session.getConsoleView().print(String.format("Try launch program:%s with %s debugger.\n", program, archType), ConsoleViewContentType.SYSTEM_OUTPUT);
                // attach debugger
                String exe = LuaFileUtil.getPluginVirtualFile(String.format("debugger/windows/%s/Debugger.exe", archType));

                GeneralCommandLine commandLine = new GeneralCommandLine(exe);
                commandLine.setCharset( Charset.forName("UTF-8"));
                commandLine.addParameters("-m", "run", "-c", program, "-e", getEmmyLua(), "-w", workingDir);
                if (args != null) {
                    String argString = String.join(" ", args);
                    if (!argString.isEmpty()) {
                        commandLine.addParameters("-a", argString);
                    }
                }

                handler = new OSProcessHandler(commandLine);
                handler.addProcessListener(processListener);
                handler.startNotify();

                writer = new BufferedWriter(new OutputStreamWriter(handler.getProcess().getOutputStream()));
            }
        } catch (Exception e) {
            session.getConsoleView().print(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
            session.stop();
        }
    }

    void stop() {
        stop(true);
    }

    void stop(boolean detach) {
        if (detach)
            send("detach");
        writer = null;
        if (handler != null) {
            handler.destroyProcess();
            handler = null;
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
            Document document = documentBuilder.parse(new ByteArrayInputStream(data.getBytes("UTF-8")));
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

    void addBreakpoint(int index, XLineBreakpoint breakpoint) {
        XExpression expression = breakpoint.getConditionExpression();
        String exp = expression != null ? expression.getExpression() : "";
        send(String.format("setb %d %d %s", index, breakpoint.getLine(), exp));
    }

    void removeBreakpoint(int index, XLineBreakpoint breakpoint) {
        send(String.format("delb %d %d", index, breakpoint.getLine()));
    }

    void sendDone() {
        send("done");
    }

    void sendRun() {
        send("run");
    }
}
