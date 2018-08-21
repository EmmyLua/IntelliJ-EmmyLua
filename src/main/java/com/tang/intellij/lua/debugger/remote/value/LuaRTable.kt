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

package com.tang.intellij.lua.debugger.remote.value

import com.intellij.icons.AllIcons
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.*
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.tang.intellij.lua.debugger.remote.LuaMobDebugProcess
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import java.util.*

/**
 *
 * Created by tangzx on 2017/4/16.
 */
class LuaRTable(name: String) : LuaRValue(name) {
    private var list: XValueChildrenList? = null
    private val desc = "table"
    private var data: LuaValue? = null

    override fun parse(data: LuaValue, desc: String) {
        this.data = data
    }

    override fun computePresentation(xValueNode: XValueNode, xValuePlace: XValuePlace) {
        xValueNode.setPresentation(AllIcons.Json.Object, "table", desc, true)
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

            return buildString {
                append(name)
                for (i in properties.indices.reversed()) {
                    val parentName = properties[i]
                    when {
                        parentName.startsWith("[") -> append(parentName)
                        parentName.matches("[0-9]+".toRegex()) -> append("[$parentName]")
                        else -> append(String.format("[\"%s\"]", parentName))
                    }
                }
            }
        }

    override fun computeChildren(node: XCompositeNode) {
        if (list == null) {
            val process = session.debugProcess as LuaMobDebugProcess
            process.evaluator?.evaluate(evalExpr, object : XDebuggerEvaluator.XEvaluationCallback {
                override fun errorOccurred(err: String) {
                    node.setErrorMessage(err)
                }

                override fun evaluated(tableValue: XValue) {
                    //////////tmp solution,非栈顶帧处理
                    var tableValue = tableValue
                    if (data != null && !(process.session as XDebugSessionImpl).isTopFrameSelected)
                        tableValue = LuaRValue.create(myName, data as LuaValue, myName, process.session)
                    //////////

                    val list = XValueChildrenList()
                    val tbl = tableValue as? LuaRTable ?: return
                    val table = tbl.data?.checktable()
                    if (table != null)
                        for (key in table.keys()) {
                            val value = LuaRValue.create(key.toString(), table.get(key), "", session)
                            value.parent = this@LuaRTable
                            list.add(value)
                        }
                    node.addChildren(list, true)
                    this@LuaRTable.list = list
                }
            }, null)
        } else
            node.addChildren(list!!, true)
    }
}