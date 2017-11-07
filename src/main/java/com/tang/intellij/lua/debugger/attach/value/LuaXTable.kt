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
import com.tang.intellij.lua.debugger.attach.DMRespEvaluate
import com.tang.intellij.lua.debugger.attach.LuaAttachBridgeBase
import com.tang.intellij.lua.debugger.attach.LuaAttachStackFrame
import org.w3c.dom.Node
import java.util.*

/**
 *
 * Created by tangzx on 2017/4/2.
 */
open class LuaXTable : LuaXValue() {

    private var childrenList: XValueChildrenList? = null
    private val functionList: LuaXFunctionList = LuaXFunctionList()

    private data class XValueItem(val name:String, val node: LuaXValue)

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
        xValueNode.setPresentation(AllIcons.Json.Object, "table", "", true)
    }

    override fun doParse(node: Node) {
        val list = mutableListOf<XValueItem>()
        val childNodes = node.childNodes
        for (i in 0 until childNodes.length) {
            val item = childNodes.item(i)
            when (item.nodeName) {
                "element" -> parseChild(item, list)
            }
        }
        if (!functionList.isEmpty())
            childrenList?.add(functionList.name, functionList)
        list.sortBy { it.name }
        list.forEach { childrenList?.add(it.name, it.node) }
    }

    private fun parseChild(childNode: Node, list: MutableList<XValueItem>) {
        if (childrenList == null)
            childrenList = XValueChildrenList()

        val childNodes = childNode.childNodes
        var key: String? = null
        var value: LuaXValue? = null
        for (i in 0 until childNodes.length) {
            val item = childNodes.item(i)
            val content = item.firstChild
            when (item.nodeName) {
                "key" -> {
                    val keyV = LuaXValue.parse(content, L, process!!)
                    key = keyV.toKeyString()
                }
                "data" -> {
                    value = LuaXValue.parse(content, L, process!!)
                    value.parent = this
                }
            }
        }

        if (key != null && value != null) {
            value.name = key
            if (value is LuaXFunction)
                functionList.add(value)
            else
                list.add(XValueItem(key, value))
        }
    }

    override fun computeChildren(node: XCompositeNode) {
        if (childrenList == null) {
            val frame = process!!.session.currentStackFrame as LuaAttachStackFrame? ?: return

            process?.bridge?.eval(L, evalExpr, frame.stack, 2, object : LuaAttachBridgeBase.EvalCallback {
                override fun onResult(result: DMRespEvaluate) {
                    val value = result.xValue
                    childrenList = XValueChildrenList()
                    if (value is LuaXTable) {
                        if (value.childrenList != null) {
                            for (i in 0 until value.childrenList!!.size()) {
                                val child = value.childrenList!!.getValue(i) as LuaXValue
                                child.parent = this@LuaXTable
                                childrenList!!.add(child.name, child)
                            }
                        }
                    }
                    node.addChildren(childrenList!!, true)
                }

            })
        } else
            node.addChildren(childrenList!!, true)
    }
}
