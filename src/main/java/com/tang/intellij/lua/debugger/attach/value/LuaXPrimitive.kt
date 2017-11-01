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

import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.tang.intellij.lua.debugger.LuaXNumberPresentation
import com.tang.intellij.lua.debugger.LuaXStringPresentation
import com.tang.intellij.lua.debugger.LuaXValuePresentation
import com.tang.intellij.lua.highlighting.LuaHighlightingData
import org.w3c.dom.Node

/**
 *
 * Created by tangzx on 2017/4/2.
 */
class LuaXPrimitive : LuaXValue() {
    private var type: String? = null
    private var data: String? = null

    override fun doParse(node: Node) {
        super.doParse(node)
        val childNodes = node.childNodes
        for (i in 0 until childNodes.length) {
            val item = childNodes.item(i)
            when (item.nodeName) {
                "type" -> type = item.textContent
                "data" -> data = item.textContent
            }
        }
    }

    override fun computePresentation(xValueNode: XValueNode, xValuePlace: XValuePlace) {
        when (type) {
            "boolean" -> xValueNode.setPresentation(null, LuaXValuePresentation(type!!, data!!, LuaHighlightingData.PRIMITIVE_TYPE), false)
            "number" -> xValueNode.setPresentation(null, LuaXNumberPresentation(data!!), false)
            "string" -> {
                var value = data
                if (value!!.startsWith("\""))
                    value = value.substring(1, value.length - 1)

                xValueNode.setPresentation(null, LuaXStringPresentation(value), false)
            }
            else -> xValueNode.setPresentation(null, type, data!!, false)
        }
    }

    override fun toKeyString(): String {
        return data!!
    }
}
