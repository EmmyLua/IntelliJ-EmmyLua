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
import com.tang.intellij.lua.lang.LuaIcons;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

/**
 * function
 * Created by tangzx on 2017/4/2.
 */
public class LuaXFunction extends LuaXValue {

    private int line;
    private int script;

    @Override
    public void doParse(Node node) {
        super.doParse(node);
        Node child = node.getFirstChild();
        while (child != null) {
            switch (child.getNodeName()) {
                case "line": line = Integer.parseInt(child.getTextContent()); break;
                case "script": script = Integer.parseInt(child.getTextContent()); break;
            }
            child = child.getNextSibling();
        }
    }

    @Override
    public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace) {
        String info;
        if (line >= 0 && script >= 0) {
            info = String.format("line:%d, script:%d", getLine(), getScript());
        } else {
            info = "native";
        }
        xValueNode.setPresentation(LuaIcons.LOCAL_FUNCTION, info, "function", false);
    }

    public int getLine() {
        return line;
    }

    public int getScript() {
        return script;
    }
}
