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

package com.tang.intellij.lua.debugger.attach

import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import java.awt.BorderLayout
import java.util.*
import javax.swing.JPanel

class ProfilerPanel(val process: LuaAttachDebugProcessBase) : JPanel(BorderLayout()) {

    inner class CallColumnInfo(name: String, val index: Int) : ColumnInfo<DMProfilerCall, String>(name) {

        private val comparator = Comparator<DMProfilerCall> { o1, o2 ->
            return@Comparator when (index) {
                0 -> o1.functionName.compareTo(o2.functionName)
                1 -> {
                    var r = o1.file.compareTo(o2.file)
                    if (r == 0) r = o1.line.compareTo(o2.line)
                    r
                }
                2 -> o1.line.compareTo(o2.line)
                3 -> o1.count.compareTo(o2.count)
                4 -> o1.time.compareTo(o2.time)
                else -> 0
            }
        }

        override fun valueOf(call: DMProfilerCall): String {
            return when (index) {
                0 -> call.functionName
                1 -> call.file
                2 -> call.line.toString()
                3 -> call.count.toString()
                4 -> (call.time.toFloat() / call.count.toFloat()).toString()
                else -> "unknown"
            }
        }

        override fun getComparator() = comparator
    }

    inner class ProfilerModel : ListTableModel<DMProfilerCall>(
            CallColumnInfo("Function", 0),
            CallColumnInfo("File", 1),
            CallColumnInfo("Line", 2),
            CallColumnInfo("Count", 3),
            CallColumnInfo("Average time", 4)
    ) {
        private val list = mutableListOf<DMProfilerCall>()

        init {
            items = list
        }

        fun update(call: DMProfilerCall) {
            for (i in 0 until list.size) {
                val c = list[i]
                if (c.id == call.id) {
                    c.count = call.count
                    c.time = call.time
                    fireTableRowsUpdated(i, i)
                    return
                }
            }
            addRow(call)
        }
    }

    private val model = ProfilerModel()
    private val table = JBTable(model)

    init {
        add(ScrollPaneFactory.createScrollPane(table), BorderLayout.CENTER)
    }

    fun updateProfiler(message: DMRespProfilerData) {
        model.update(message.list[0])
    }
}