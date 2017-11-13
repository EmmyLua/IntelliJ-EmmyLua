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

import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcessBase

class LuaXMultiValue(L: Long, process: LuaAttachDebugProcessBase)
    : LuaXValue(L, process) {

    init {
        name = "multiple values"
    }

    private val list = mutableListOf<LuaXValue>()

    fun addChild(node: LuaXValue) {
        node.name = "[${list.size + 1}]"
        list.add(node)
    }

    override fun computePresentation(xValueNode: XValueNode, xValuePlace: XValuePlace) {
        xValueNode.setPresentation(null, name, "${list.size} item(s)", true)
    }

    override fun computeChildren(node: XCompositeNode) {
        val ret = XValueChildrenList()
        list.forEach { ret.add(it.name, it) }
        node.addChildren(ret, true)
    }
}