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

package com.tang.intellij.lua.debugger.emmy

import com.intellij.ui.ColoredTextContainer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.tang.intellij.lua.debugger.emmy.value.LuaXValue
import com.tang.intellij.lua.psi.LuaFileUtil

class EmmyDebugStackFrame(val data: Stack, val process: EmmyDebugProcessBase) : XStackFrame() {
    private val values = XValueChildrenList()
    private var evaluator: EmmyEvaluator? = null
    private val sourcePosition by lazy {
        val file = LuaFileUtil.findFile(process.session.project, data.file)
        if (file == null) null else XSourcePositionImpl.create(file, data.line - 1)
    }

    init {
        data.localVariables.forEach {
            addValue(LuaXValue.create(it, this))
        }
        data.upvalueVariables.forEach {
            addValue(LuaXValue.create(it, this))
        }
    }

    override fun getEvaluator(): EmmyEvaluator? {
        if (evaluator == null)
            evaluator = EmmyEvaluator(this, process)
        return evaluator
    }

    override fun customizePresentation(component: ColoredTextContainer) {
        component.append("${data.file}:${data.functionName}:${data.line}", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    private fun addValue(node: LuaXValue) {
        values.add(node.name, node)
    }

    override fun computeChildren(node: XCompositeNode) {
        node.addChildren(values, true)
    }

    override fun getSourcePosition(): XSourcePosition? {
        return sourcePosition
    }
}