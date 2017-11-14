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

import com.intellij.openapi.project.Project
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JPanel

data class LuaVM(val id: Long)

class LuaVMPanel(val project: Project) : JPanel(BorderLayout()) {
    private val model = DefaultListModel<LuaVM>()

    init {
        val list = JBList<LuaVM>(model)
        list.installCellRenderer<LuaVM> { d ->
            val label = JLabel("0x${java.lang.Long.toHexString(d.id)}")
            label
        }
        add(ScrollPaneFactory.createScrollPane(list), BorderLayout.CENTER)
    }

    fun addVM(message: LuaAttachMessage) {
        model.addElement(LuaVM(message.L))
    }

    fun removeVM(message: LuaAttachMessage) {
        findVM(message.L)?.let {
            model.removeElement(it)
        }
    }

    private fun findVM(L: Long): LuaVM? {
        val enumeration = model.elements()
        while (enumeration.hasMoreElements()) {
            val vm = enumeration.nextElement()
            if (vm.id == L) {
                return vm
            }
        }
        return null
    }
}
