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

import com.intellij.execution.ui.ConsoleViewContentType;
import org.w3c.dom.Node;

/**
 * message
 * Created by tangzx on 2017/4/2.
 */
public class LuaAttachMessageProto extends LuaAttachProto {
    private String message;
    private int messageType = Normal;

    public static final int Normal          = 0;
    public static final int Warning         = 1;
    public static final int Error           = 2;

    public LuaAttachMessageProto(int type) {
        super(type);
    }

    @Override
    protected void eachData(Node item) {
        super.eachData(item);
        if (item.getNodeName().equals("message")) {
            this.message = item.getTextContent();
        } else if (item.getNodeName().equals("message_type")) {
            this.messageType = Integer.parseInt(item.getTextContent());
        }
    }

    public String getMessage() {
        return message;
    }

    public void outputToConsole() {
        ConsoleViewContentType contentType = ConsoleViewContentType.SYSTEM_OUTPUT;
        if (messageType == Normal)
            contentType = ConsoleViewContentType.NORMAL_OUTPUT;
        else if (messageType == Warning)
            contentType = ConsoleViewContentType.SYSTEM_OUTPUT;
        else if (messageType == Error)
            contentType = ConsoleViewContentType.ERROR_OUTPUT;
        getProcess().print(message + '\n', contentType);
    }
}
