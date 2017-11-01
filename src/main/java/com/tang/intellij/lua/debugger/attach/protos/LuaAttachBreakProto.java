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
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.tang.intellij.lua.debugger.LuaExecutionStack;
import com.tang.intellij.lua.debugger.attach.LoadedScript;
import com.tang.intellij.lua.debugger.attach.LuaAttachStackFrame;
import com.tang.intellij.lua.debugger.attach.value.LuaXValue;
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
                parseStack(item);
                break;
        }
    }

    private void parseStack(Node item) {
        List<XStackFrame> frames = new ArrayList<>();
        Node stackNode = item.getFirstChild();
        int stackIndex = 0;
        while (stackNode != null) {
            NamedNodeMap attributes = stackNode.getAttributes();
            Node functionNode = attributes.getNamedItem("function");
            Node scriptIndexNode = attributes.getNamedItem("script_index");
            Node lineNode = attributes.getNamedItem("line");

            LoadedScript script = getProcess().getScript(Integer.parseInt(scriptIndexNode.getTextContent()));
            String scriptName = null;
            int line = Integer.parseInt(lineNode.getTextContent());
            XSourcePosition position = null;
            if (script != null) {
                scriptName = script.getName();
                // find source position
                VirtualFile file = LuaFileUtil.findFile(getProcess().getSession().getProject(), scriptName);
                if (file != null) {
                    position = XSourcePositionImpl.create(file, line);

                    if (name == null) {
                        this.line = line;
                        this.name = scriptName;
                    }
                }
            }
            XValueChildrenList childrenList = parseValue(stackNode);
            //LuaAttachStackFrame frame = new LuaAttachStackFrame(this, childrenList, position, functionNode.getTextContent(), scriptName, stackIndex);
            //frames.add(frame);

            stackIndex++;
            stackNode = stackNode.getNextSibling();
        }
        stack = new LuaExecutionStack(frames);
    }

    private XValueChildrenList parseValue(Node stackNode) {
        XValueChildrenList list = new XValueChildrenList();
        Node valueNode = stackNode.getFirstChild();
        while (valueNode != null) {
            LuaXValue value = LuaXValue.Companion.parse(valueNode, getProcess());
            if (value != null) {
                String name = "unknown";
                NodeList valueNodeChildNodes = valueNode.getChildNodes();
                for (int i = 0; i < valueNodeChildNodes.getLength(); i++) {
                    Node item = valueNodeChildNodes.item(i);
                    if (item.getNodeName().equals("name")) {
                        name = item.getTextContent();
                        break;
                    }
                }
                value.setName(name);
                list.add(name, value);
            }
            valueNode = valueNode.getNextSibling();
        }
        return list;
    }

    public int getLine() {
        return line;
    }

    public String getName() {
        return name;
    }
}
