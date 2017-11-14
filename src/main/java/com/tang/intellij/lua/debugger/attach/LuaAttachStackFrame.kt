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

package com.tang.intellij.lua.debugger.attach

import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTextContainer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XValueChildrenList

/**
 *
 * Created by tangzx on 2017/4/2.
 */
class LuaAttachStackFrame(val proto: DMBreak,
                          private val childrenList: XValueChildrenList,
                          private val position: XSourcePosition?,
                          private val function: String?,
                          private val scriptName: String?,
                          val stack: Int) : XStackFrame() {
    private val process: LuaAttachDebugProcessBase = proto.process
    private var evaluator: LuaAttachDebuggerEvaluator? = null

    override fun computeChildren(node: XCompositeNode) {
        node.addChildren(childrenList, true)
    }

    override fun customizePresentation(component: ColoredTextContainer) {
        if (function != null) {
            component.append(function, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            if (scriptName != null)
                component.append(String.format("(%s)", scriptName), SimpleTextAttributes.GRAY_ATTRIBUTES)
        } else {
            component.append("invalid", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
        component.setIcon(AllIcons.Debugger.StackFrame)
    }

    override fun getEvaluator(): XDebuggerEvaluator? {
        if (evaluator == null && scriptName != null) {
            evaluator = LuaAttachDebuggerEvaluator(process, this)
        }
        return evaluator
    }

    override fun getSourcePosition(): XSourcePosition? {
        return position
    }
}
