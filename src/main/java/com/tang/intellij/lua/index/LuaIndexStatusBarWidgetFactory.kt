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

package com.tang.intellij.lua.index

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.ui.components.JBLabel
import javax.swing.JComponent

class LuaIndexStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "luaIndexStatus"

    override fun getDisplayName(): String = "Lua Index Status"

    override fun createWidget(project: Project): StatusBarWidget {
        return LuaIndexStatusBarWidget(project)
    }
}

class LuaIndexStatusBarWidget(project: Project) : JBLabel(), CustomStatusBarWidget, LuaIndexListener {
    override fun ID(): String = "LuaIndexStatusBarWidget"

    override fun getComponent(): JComponent = this

    private var _startAt = 0L
    private var _delayVisible = false

    init {
        this.text = "Lua status: None"

        val connection = project.messageBus.connect()
        connection.subscribe(IndexManager.TOPIC, this)
    }

    override fun onStatus(status: LuaIndexStatus, complete: Int, total: Int) {
        runReadAction {
            this.text = when (status) {
                LuaIndexStatus.Waiting -> "Lua status: None"
                LuaIndexStatus.Collecting -> "Lua status: Collecting"
                LuaIndexStatus.Analyse -> "Lua status: Analyse(${complete}/${total})"
                LuaIndexStatus.Finished -> "Lua status: Finished"
            }
            val isVisible = status != LuaIndexStatus.Finished
            if (isVisible) {
                if (_delayVisible) {
                    // delay show
                    val dt = System.currentTimeMillis() - _startAt
                    if (dt >= 100) this.isVisible = true
                } else {
                    _delayVisible = true
                    _startAt = System.currentTimeMillis()
                }
            }
            if (!isVisible && _delayVisible) {
                _delayVisible = false
                this.isVisible = false
            }
        }
    }
}