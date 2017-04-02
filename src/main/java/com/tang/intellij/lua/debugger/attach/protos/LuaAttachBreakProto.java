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

package com.tang.intellij.lua.debugger.attach.protos;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.tang.intellij.lua.debugger.LuaExecutionStack;
import com.tang.intellij.lua.debugger.attach.LuaAttackStackFrame;
import com.tang.intellij.lua.psi.LuaFileUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public class LuaAttachBreakProto extends LuaAttachProto {
    private int line;
    private String name;
    private LuaExecutionStack stack;

    public LuaAttachBreakProto() {
        super(Break);
    }

    public LuaExecutionStack getStack() {
        return stack;
    }

    @Override
    protected void eachData(Node item) {
        super.eachData(item);
        String name = item.getNodeName();
        switch (name) {
            case "name":
                this.name = item.getTextContent();
                break;
            case "line":
                this.line = Integer.parseInt(item.getTextContent());
                break;
            case "stacks":
                NodeList stackNodes = item.getChildNodes();
                List<XStackFrame> frames = new ArrayList<>();
                for (int i = 0; i < stackNodes.getLength(); i++) {
                    Node stackNode = stackNodes.item(i);
                    NamedNodeMap stackFrameAttrs = stackNode.getAttributes();
                    Node function = stackFrameAttrs.getNamedItem("function");
                    Node scriptNameNode = stackFrameAttrs.getNamedItem("script_name");
                    Node lineNode = stackFrameAttrs.getNamedItem("line");
                    String scriptName = null;
                    XSourcePosition position = null;
                    if (scriptNameNode != null) {
                        scriptName = scriptNameNode.getTextContent();
                        // find source position
                        VirtualFile file = LuaFileUtil.findFile(getProcess().getSession().getProject(), scriptName);
                        if (file != null) {
                            position = XSourcePositionImpl.create(file, Integer.parseInt(lineNode.getTextContent()));
                        }
                    }
                    LuaAttackStackFrame frame = new LuaAttackStackFrame(getProcess(), position, function.getTextContent(), scriptName);
                    frames.add(frame);
                }
                stack = new LuaExecutionStack(frames);
                break;
        }
    }

    public int getLine() {
        return line;
    }

    public String getName() {
        return name;
    }
}
