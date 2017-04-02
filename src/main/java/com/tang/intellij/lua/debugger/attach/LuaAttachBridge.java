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
import com.tang.intellij.lua.debugger.attach.protos.*;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
    private ProtoHandler protoHandler;

    void setProtoHandler(ProtoHandler protoHandler) {
        this.protoHandler = protoHandler;
    }

    public interface ProtoHandler {
        void handle(LuaAttachProto proto);
    }

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
                    LuaAttachProto proto = parse(s);
                    System.out.println(s);
                    if (protoHandler != null && proto != null)
                        protoHandler.handle(proto);
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

    public void send(String data) {
        try {
            writer.write(data);
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
    }

    private LuaAttachProto createProto(int type, Element root) throws Exception {
        LuaAttachProto proto;
        switch (type) {
            case LuaAttachProto.Message:
                proto = new LuaAttachMessageProto();
                break;
            case LuaAttachProto.LoadScript:
                proto = new LuaAttachLoadScriptProto();
                break;
            case LuaAttachProto.SetBreakpoint:
                proto = new LuaAttachSetBreakpointProto();
                break;
            case LuaAttachProto.Break:
                proto = new LuaAttachBreakProto();
                break;
            default:
                proto = new LuaAttachProto(type);
        }
        proto.doParse(root);
        return proto;
    }
}
