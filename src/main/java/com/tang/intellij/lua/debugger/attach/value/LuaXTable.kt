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

package com.tang.intellij.lua.debugger.attach.value

import com.intellij.icons.AllIcons
import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.tang.intellij.lua.debugger.attach.*
import java.io.DataInputStream
import java.util.*

/**
 *
 * Created by tangzx on 2017/4/2.
 */
open class LuaXTable(L: Long, process: LuaAttachDebugProcessBase)
    : LuaXObjectValue(StackNodeId.Table, L, process), IStackNode {

    private val children = mutableListOf<LuaXValue>()
    private var childrenList: XValueChildrenList? = null
    private val functionList: LuaXFunctionList = LuaXFunctionList(L, process)
    private var deep = false

    private val evalExpr: String
        get() {
            var name = name
            val properties = ArrayList<String>()
            var parent = this.parent
            while (parent != null) {
                val parentName = parent.name
                if (parentName == null)
                    break
                else {
                    properties.add(name!!)
                    name = parentName
                    parent = parent.parent
                }
            }

            val sb = StringBuilder(name!!)
            for (i in properties.indices.reversed()) {
                val parentName = properties[i]
                if (parentName.startsWith("["))
                    sb.append(parentName)
                else
                    sb.append(String.format("[\"%s\"]", parentName))
            }
            return sb.toString()
        }

    override fun computePresentation(xValueNode: XValueNode, xValuePlace: XValuePlace) {
        val type = if (this.type.isEmpty()) "table" else type
        xValueNode.setPresentation(AllIcons.Json.Object, type, data, true)
    }

    override fun read(stream: DataInputStream) {
        super.read(stream)
        deep = stream.readBoolean()
        if (deep) {
            val size = stream.readSize()
            for (i in 0 until size) {
                val key = readNode(stream, L, process) as LuaXValue
                val value = readNode(stream, L, process) as LuaXValue
                val name = key.toKeyString()
                value.name = name
                add(value)
            }
            sort()
        }
    }

    private fun add(value: LuaXValue) {
        value.parent = this
        children.add(value)
    }

    private fun sort() {
        deep = true
        val sortList = mutableListOf<LuaXValue>()
        children.forEach {
            if (it is LuaXFunction) {
                functionList.add(it)
            } else sortList.add(it)
        }
        val list = XValueChildrenList()
        if (!functionList.isEmpty())
            list.add(functionList.name, functionList)
        sortList.sortBy { it.name }
        sortList.forEach { list.add(it.name, it) }
        childrenList = list
    }

    override fun computeChildren(node: XCompositeNode) {
        if (childrenList == null) {
            val frame = process.session.currentStackFrame as? LuaAttachStackFrame ?: return

            process.bridge.eval(L, evalExpr, frame.stack, 2, object : LuaAttachBridgeBase.EvalCallback {
                override fun onResult(result: DMRespEvaluate) {
                    val value = result.resultNode.value
                    if (value is LuaXTable) {
                        value.children.forEach {
                            add(it)
                        }
                    }
                    sort()
                    node.addChildren(childrenList!!, true)
                }
            })
        } else
            node.addChildren(childrenList!!, true)
    }

    override fun toKeyString(): String {
        return "[table]"
    }
}
