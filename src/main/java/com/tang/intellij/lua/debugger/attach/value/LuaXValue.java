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

package com.tang.intellij.lua.debugger.attach.value;

import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public abstract class LuaXValue extends XValue {
    protected LuaAttachDebugProcess process;

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {

    }

    public void doParse(Node node) {

    }

    public String toKeyString() {
        return toString();
    }

    public static LuaXValue parse(String data, LuaAttachDebugProcess process) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(data.getBytes()));
            Element root = document.getDocumentElement();
            return parse(root, process);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LuaXValue parse(Node node, LuaAttachDebugProcess process) {
        String nodeName = node.getNodeName();
        LuaXValue value = null;
        switch (nodeName) {
            case "table":
                value = new LuaXTable();
                break;
            case "value":
                value = new LuaXString();
                break;
            case "function":
                value = new LuaXFunction();
                break;
        }
        if (value != null) {
            value.setProcess(process);
            value.doParse(node);
        }
        return value;
    }

    public void setProcess(LuaAttachDebugProcess process) {
        this.process = process;
    }
}
