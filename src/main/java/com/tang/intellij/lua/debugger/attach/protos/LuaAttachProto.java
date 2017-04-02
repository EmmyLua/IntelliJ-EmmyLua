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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * lua attach debugger proto
 * Created by tangzx on 2017/4/2.
 */
public class LuaAttachProto {
    public static final int Initialize          = 11;   // Sent when the backend is ready to have its initialize function called
    public static final int CreateVM            = 1;    // Sent when a script VM is created.
    public static final int DestroyVM           = 2;    // Sent when a script VM is destroyed.
    public static final int LoadScript          = 3;    // Sent when script data is loaded into the VM.
    public static final int Break               = 4;    // Sent when the debugger breaks on a line.
    public static final int SetBreakpoint       = 5;    // Sent when a breakpoint has been added in the debugger.
    public static final int Exception           = 6;    // Sent when the script encounters an exception (e.g. crash).
    public static final int LoadError           = 7;    // Sent when there is an error loading a script (e.g. syntax error).
    public static final int Message             = 9;    // Event containing a string message from the debugger.
    public static final int SessionEnd          = 8;    // This is used internally and shouldn't be sent.
    public static final int NameVM              = 10;   // Sent when the name of a VM is set.

    private int type;

    public LuaAttachProto(int type) {
        this.type = type;
    }

    public void doParse(Element data) throws Exception {
        NodeList childNodes = data.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            eachData(item);
        }
    }

    protected void eachData(Node item) {}

    public int getType() {
        return type;
    }
}
