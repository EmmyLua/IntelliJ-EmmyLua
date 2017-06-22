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

package com.tang.intellij.lua.debugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.impl.frame.XStackFrameContainerEx;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaExecutionStack extends XExecutionStack {
    private XStackFrame topFrame;
    private List<XStackFrame> stackFrameList;

    public LuaExecutionStack(List<XStackFrame> stackFrameList) {
        super("LuaStack");
        this.stackFrameList = stackFrameList;
        if (!stackFrameList.isEmpty())
            topFrame = stackFrameList.get(0);
    }

    @Nullable
    @Override
    public XStackFrame getTopFrame() {
        return topFrame;
    }

    public void setTopFrame(XStackFrame frame) {
        topFrame = frame;
    }

    public XStackFrame[] getStackFrames() {
        return stackFrameList.toArray(new XStackFrame[stackFrameList.size()]);
    }

    @Override
    public void computeStackFrames(int i, XStackFrameContainer xStackFrameContainer) {
        XStackFrameContainerEx stackFrameContainerEx = (XStackFrameContainerEx) xStackFrameContainer;
        stackFrameContainerEx.addStackFrames(stackFrameList, topFrame, true);
    }
}
