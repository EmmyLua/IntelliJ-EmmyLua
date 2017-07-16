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

package com.tang.intellij.lua.debugger.remote;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class LuaMobStackFrame extends XStackFrame {

    private String functionName;
    private XSourcePosition position;
    private LuaMobDebugProcess process;
    private XValueChildrenList values = new XValueChildrenList();

    public LuaMobStackFrame(String functionName, XSourcePosition position, LuaMobDebugProcess debugProcess) {
        this.functionName = functionName;
        this.position = position;
        process = debugProcess;
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        return  new LuaMobDebuggerEvaluator(process);
    }

    @Nullable
    @Override
    public XSourcePosition getSourcePosition() {
        return position;
    }

    public void addValue(XNamedValue namedValue) {
        values.add(namedValue);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        node.addChildren(values, true);
    }

    public void customizePresentation(@NotNull ColoredTextContainer component) {
        XSourcePosition position = this.getSourcePosition();
        String info = functionName;
        String positionInfo = "unknown";
        if(position != null) {
            positionInfo = position.getFile().getName() + ":" + (position.getLine() + 1);
        }

        if (functionName != null)
            info = String.format("%s (%s)", functionName, positionInfo);
        component.append(info, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        component.setIcon(AllIcons.Debugger.StackFrame);
    }
}
