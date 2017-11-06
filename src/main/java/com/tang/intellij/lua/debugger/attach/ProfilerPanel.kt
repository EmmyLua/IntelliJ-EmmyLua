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
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.AbstractTableModel

class ProfilerPanel : JPanel(BorderLayout()) {

    inner class ProfilerModel : AbstractTableModel() {

        private val names = arrayOf("Function", "File", "Line", "Count", "Average time")

        private val list = mutableListOf<DMProfilerCall>()

        override fun getColumnName(column: Int) = names[column]

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
            add(call)
        }

        fun add(call: DMProfilerCall) {
            list.add(call)
            fireTableRowsInserted(list.size - 1, list.size - 1)
        }

        override fun getRowCount(): Int {
            return list.size
        }

        override fun getColumnCount(): Int {
            return 5
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val call = list[rowIndex]
            return when (columnIndex) {
                0 -> call.functionName
                1 -> call.file
                2 -> call.line
                3 -> call.count
                4 -> (call.time.toFloat() / call.count.toFloat())
                else -> "unknown"
            }
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