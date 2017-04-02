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

package com.tang.intellij.lua.debugger.attach;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by tangzx on 2017/4/2.
 */
public class LuaAttackStackFrame extends XStackFrame {
    private LuaAttachDebugProcess process;
    private XSourcePosition position;
    private String function;
    private String scriptName;
    private LuaAttackDebuggerEvaluator evaluator;

    public LuaAttackStackFrame(LuaAttachDebugProcess process, XSourcePosition position, String function, String scriptName) {
        this.process = process;
        this.position = position;
        this.function = function;
        this.scriptName = scriptName;
    }

    @Override
    public void customizePresentation(@NotNull ColoredTextContainer component) {
        if (function != null) {
            component.append(function, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            if (scriptName != null)
                component.append(scriptName, SimpleTextAttributes.GRAY_ATTRIBUTES);
        } else {
            component.append("invalid", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        component.setIcon(AllIcons.Debugger.StackFrame);
    }

    @Nullable
    @Override
    public XDebuggerEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = new LuaAttackDebuggerEvaluator(process);
        }
        return evaluator;
    }

    @Nullable
    @Override
    public XSourcePosition getSourcePosition() {
        return position;
    }
}
