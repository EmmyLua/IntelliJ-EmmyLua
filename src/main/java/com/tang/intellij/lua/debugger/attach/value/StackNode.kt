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

import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcessBase
import com.tang.intellij.lua.debugger.attach.readSize
import com.tang.intellij.lua.debugger.attach.readString
import java.io.DataInputStream

interface IStackNode {
    val L: Long
    val process: LuaAttachDebugProcessBase
    fun read(stream: DataInputStream)
}

private val map = mutableMapOf<Byte, StackNodeId>()
private fun initMap() {
    StackNodeId.values().forEach { map[it.ordinal.toByte()] = it }
}

private fun getNode(byte: Byte): StackNodeId {
    if (map.isEmpty()) initMap()
    val id = map[byte]
    return id!!
}

fun readNode(stream: DataInputStream, L: Long, process: LuaAttachDebugProcessBase): IStackNode {
    val id = getNode(stream.readByte())
    val value: IStackNode = when (id) {
        StackNodeId.List -> StackNodeContainer(L, process)
        StackNodeId.Eval -> EvalResultNode(L, process)
        StackNodeId.StackRoot -> StackRootNode(L, process)

        StackNodeId.Table -> LuaXTable(L, process)
        StackNodeId.Function -> LuaXFunction(L, process)
        StackNodeId.UserData -> LuaXUserdata(L, process)

        StackNodeId.Error -> LuaXString(L, process)
        StackNodeId.String -> LuaXString(L, process)
        StackNodeId.Primitive -> LuaXPrimitive(L, process)

        else -> throw Exception("unknown stack node id : $id")
    }
    value.read(stream)
    return value
}

open class StackNodeContainer(override var L: Long,
                              override var process: LuaAttachDebugProcessBase) : IStackNode {

    val children = mutableListOf<IStackNode>()

    override fun read(stream: DataInputStream) {
        val size = stream.readSize()
        for (i in 0 until size) {
            val node = readNode(stream, L, process)
            children.add(node)
        }
    }
}

class EvalResultNode(L: Long, process: LuaAttachDebugProcessBase) : StackNodeContainer(L, process) {

    var success: Boolean = false
    var error: String = ""

    override fun read(stream: DataInputStream) {
        super.read(stream)
        success = stream.readBoolean()
        if (!success)
            error = stream.readString(process.charset)
    }

    val value: LuaXValue get() {
        return if (children.size == 1)
            children[0] as LuaXValue
        else {
            val mv = LuaXMultiValue(L, process)
            children.forEach { mv.addChild(it as LuaXValue) }
            mv
        }
    }
}

class StackRootNode(L: Long, process: LuaAttachDebugProcessBase) : StackNodeContainer(L, process) {
    var scriptIndex = 0
    var line = 0
    var functionName = ""

    override fun read(stream: DataInputStream) {
        super.read(stream)
        scriptIndex = stream.readInt()
        line = stream.readInt()
        functionName = stream.readString(process.charset)
    }

}