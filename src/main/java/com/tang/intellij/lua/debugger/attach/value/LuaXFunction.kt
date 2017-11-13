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
import com.tang.intellij.lua.debugger.attach.LuaAttachDebugProcessBase
import com.tang.intellij.lua.lang.LuaIcons
import java.io.DataInputStream

/**
 * function
 * Created by tangzx on 2017/4/2.
 */
class LuaXFunction(L: Long, process: LuaAttachDebugProcessBase)
    : LuaXObjectValue(StackNodeId.Function, L, process) {

    var line: Int = 0
        private set
    private var script: Int = 0

    override fun read(stream: DataInputStream) {
        super.read(stream)
        script = stream.readInt()
        line = stream.readInt()
    }

    /*override fun doParse(node: Node) {
        super.doParse(node)
        var child: Node? = node.firstChild
        while (child != null) {
            when (child.nodeName) {
                "line" -> line = Integer.parseInt(child.textContent)
                "script" -> script = Integer.parseInt(child.textContent)
            }
            child = child.nextSibling
        }
    }*/

    override fun computePresentation(xValueNode: XValueNode, xValuePlace: XValuePlace) {
        val info = if (line >= 0 && script >= 0) {
            val loadedScript = process.getScript(this.script)
            if (loadedScript != null) {
                String.format("line:%d, script:%s", line, loadedScript.name)
            } else {
                "unknown source"
            }
        } else {
            "native"
        }
        xValueNode.setPresentation(LuaIcons.LOCAL_FUNCTION, info, "function", false)
    }
}
