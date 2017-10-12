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

import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.tang.intellij.lua.debugger.LuaXNumberPresentation;
import com.tang.intellij.lua.debugger.LuaXStringPresentation;
import com.tang.intellij.lua.debugger.LuaXValuePresentation;
import com.tang.intellij.lua.highlighting.LuaHighlightingData;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public class LuaXPrimitive extends LuaXValue {
    private String type;
    private String data;

    @Override
    public void doParse(Node node) {
        super.doParse(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            switch (item.getNodeName()) {
                case "type":
                    type = item.getTextContent();
                    break;
                case "data":
                    data = item.getTextContent();
                    break;
            }
        }
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        switch (type) {
            case "boolean":
                xValueNode.setPresentation(null, new LuaXValuePresentation(type, data, LuaHighlightingData.INSTANCE.getPRIMITIVE_TYPE()), false);
                break;
            case "number":
                xValueNode.setPresentation(null, new LuaXNumberPresentation(data), false);
                break;
            case "string":
                String value = data;
                if (value.startsWith("\""))
                    value = value.substring(1, value.length() - 1);

                xValueNode.setPresentation(null, new LuaXStringPresentation(value), false);
                break;
            default:
                xValueNode.setPresentation(null, type, data, false);
        }
    }

    @Override
    public String toKeyString() {
        return data;
    }
}
