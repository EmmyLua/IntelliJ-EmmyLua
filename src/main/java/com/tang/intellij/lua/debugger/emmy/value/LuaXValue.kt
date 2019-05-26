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

package com.tang.intellij.lua.debugger.emmy.value

import com.intellij.icons.AllIcons
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.*
import com.tang.intellij.lua.debugger.LuaXBoolPresentation
import com.tang.intellij.lua.debugger.LuaXNumberPresentation
import com.tang.intellij.lua.debugger.LuaXStringPresentation
import com.tang.intellij.lua.debugger.emmy.EmmyDebugStackFrame
import com.tang.intellij.lua.debugger.emmy.VariableValue
import java.util.ArrayList

abstract class LuaXValue : XValue() {
    companion object {
        fun create(v: VariableValue, frame: EmmyDebugStackFrame): LuaXValue {
            return when(v.valueType) {
                "string" -> StringXValue(v)
                "number" -> NumberXValue(v)
                "boolean" -> BoolXValue(v)
                "table" -> TableXValue(v, frame)
                else -> AnyXValue(v)
            }
        }
    }

    abstract val name: String

    var parent: LuaXValue? = null
}

class StringXValue(val v: VariableValue) : LuaXValue() {

    override val name: String
        get() = v.nameValue

    override fun computePresentation(xValueNode: XValueNode, place: XValuePlace) {
        xValueNode.setPresentation(null, LuaXStringPresentation(v.value), false)
    }
}

class NumberXValue(val v: VariableValue) : LuaXValue() {

    override val name: String
        get() = v.nameValue

    override fun computePresentation(xValueNode: XValueNode, place: XValuePlace) {
        xValueNode.setPresentation(null, LuaXNumberPresentation(v.value), false)
    }
}

class BoolXValue(val v: VariableValue) : LuaXValue() {

    override val name: String
        get() = v.nameValue

    override fun computePresentation(xValueNode: XValueNode, place: XValuePlace) {
        xValueNode.setPresentation(null, LuaXBoolPresentation(v.value), false)
    }
}

class AnyXValue(val v: VariableValue) : LuaXValue() {
    override val name: String
        get() = v.nameValue

    override fun computePresentation(xValueNode: XValueNode, place: XValuePlace) {
        xValueNode.setPresentation(null, v.valueType, v.value, false)
    }
}

class TableXValue(val v: VariableValue, val frame: EmmyDebugStackFrame) : LuaXValue() {

    private val children = mutableListOf<LuaXValue>()

    init {
        v.children?.
                sortedBy { it.nameValue }?.
                forEach {
            children.add(create(it, frame))
        }
    }

    override val name: String
        get() = v.nameValue

    override fun computePresentation(xValueNode: XValueNode, place: XValuePlace) {
        xValueNode.setPresentation(AllIcons.Json.Object, v.valueType, v.value, true)
    }

    override fun computeChildren(node: XCompositeNode) {
        val ev = this.frame.evaluator
        if (ev != null) {
            ev.eval(evalExpr, object : XDebuggerEvaluator.XEvaluationCallback {
                override fun errorOccurred(err: String) {
                    node.setErrorMessage(err)
                }

                override fun evaluated(value: XValue) {
                    if (value is TableXValue) {
                        val cl = XValueChildrenList()
                        children.clear()
                        children.addAll(value.children)
                        children.forEach {
                            it.parent = this@TableXValue
                            cl.add(it.name, it)
                        }
                        node.addChildren(cl, true)
                    }
                }

            }, 2)
        }
        else super.computeChildren(node)
    }

    private val evalExpr: String
        get() {
            var name = name
            val properties = ArrayList<String>()
            var parent = this.parent
            while (parent != null) {
                val parentName = parent.name
                properties.add(name)
                name = parentName
                parent = parent.parent
            }

            val sb = StringBuilder(name)
            for (i in properties.indices.reversed()) {
                val parentName = properties[i]
                if (parentName.startsWith("["))
                    sb.append(parentName)
                else
                    sb.append(String.format("[\"%s\"]", parentName))
            }
            return sb.toString()
        }
}