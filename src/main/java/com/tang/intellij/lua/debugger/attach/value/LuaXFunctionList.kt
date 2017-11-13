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
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcessBase

class LuaXFunctionList(L: Long, process: LuaAttachDebugProcessBase)
    : LuaXValue(L, process) {
    init {
        name = "Functions"
    }

    private val list = mutableListOf<LuaXFunction>()

    override fun computePresentation(xValueNode: XValueNode, xValuePlace: XValuePlace) {
        xValueNode.setPresentation(AllIcons.Json.Object, name, "${list.size} function(s)", true)
    }

    override fun computeChildren(node: XCompositeNode) {
        val childrenList = XValueChildrenList()
        list.forEach { childrenList.add(it.name, it) }
        node.addChildren(childrenList, true)
    }

    fun add(f: LuaXFunction) {
        list.add(f)
    }

    fun isEmpty(): Boolean {
        return list.isEmpty()
    }
}