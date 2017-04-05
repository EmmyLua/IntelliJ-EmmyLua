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

import com.intellij.xdebugger.frame.*;
import com.tang.intellij.lua.debugger.attach.LuaAttachStackFrame;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public class LuaXTable extends LuaXValue {

    private XValueChildrenList childrenList;

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        xValueNode.setPresentation(null, null, "table", true);
    }

    @Override
    public void doParse(Node node) {
        super.doParse(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            switch (item.getNodeName()) {
                case "element":
                    parseChild(item);
                    break;
            }
        }
    }

    private void parseChild(Node childNode) {
        if (childrenList == null)
            childrenList = new XValueChildrenList();

        NodeList childNodes = childNode.getChildNodes();
        String key = null;
        LuaXValue value = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            Node content = item.getFirstChild();
            switch (item.getNodeName()) {
                case "key":
                    LuaXValue keyV = LuaXValue.parse(content, process);
                    key = keyV.toKeyString();
                    break;
                case "data":
                    value = LuaXValue.parse(content, process);
                    value.setParent(this);
                    break;
            }
        }

        if (key != null && value != null) {
            value.setName(key);
            childrenList.add(key, value);
        }
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (childrenList == null) {
            LuaAttachStackFrame frame = (LuaAttachStackFrame) process.getSession().getCurrentStackFrame();
            if (frame == null) return;

            process.getBridge().eval(getEvalExpr(), frame.getStack(), 2, v -> {
                XValue value = v.getXValue();
                childrenList = new XValueChildrenList();
                if (value instanceof LuaXTable) {
                    LuaXTable table = (LuaXTable) value;
                    if (table.childrenList != null) {
                        for (int i = 0; i < table.childrenList.size(); i++) {
                            LuaXValue child = (LuaXValue) table.childrenList.getValue(i);
                            child.setParent(this);
                            childrenList.add(child.getName(), child);
                        }
                    }
                }
                node.addChildren(childrenList, true);
            });
        } else node.addChildren(childrenList, true);
    }

    private String getEvalExpr() {
        String name = getName();
        ArrayList<String> properties = new ArrayList<>();
        LuaXValue parent = this.parent;
        while (parent != null) {
            String parentName = parent.getName();
            if (parentName == null)
                break;
            else {
                properties.add(name);
                name = parentName;
                parent = parent.parent;
            }
        }

        StringBuilder sb = new StringBuilder(name);
        for (int i = properties.size() - 1; i >= 0; i--) {
            String parentName = properties.get(i);
            if (parentName.startsWith("["))
                sb.append(parentName);
            else
                sb.append(String.format("[\"%s\"]", parentName));
        }
        return sb.toString();
    }
}
