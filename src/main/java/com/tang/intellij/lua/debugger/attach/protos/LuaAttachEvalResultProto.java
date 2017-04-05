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

import com.intellij.xdebugger.frame.XValue;
import com.tang.intellij.lua.debugger.attach.value.LuaXValue;
import org.w3c.dom.Node;

/**
 * eval result
 * Created by tangzx on 2017/4/2.
 */
public class LuaAttachEvalResultProto extends LuaAttachProto {

    private boolean success;
    private int evalId;
    private XValue value;

    public LuaAttachEvalResultProto() {
        super(EvalResult);
    }

    @Override
    protected void eachData(Node item) {
        super.eachData(item);
        switch (item.getNodeName()) {
            case "result":
                success = Integer.parseInt(item.getTextContent()) == 1;
                break;
            case "value":
                value = LuaXValue.parse(item.getFirstChild(), getProcess());
                break;
            case "id":
                evalId = Integer.parseInt(item.getTextContent());
                break;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public int getEvalId() {
        return evalId;
    }

    public XValue getXValue() {
        return value;
    }
}
