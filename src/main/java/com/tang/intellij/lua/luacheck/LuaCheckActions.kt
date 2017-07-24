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

package com.tang.intellij.lua.luacheck

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.options.ShowSettingsUtil
import com.tang.intellij.lua.lang.LuaFileType

class LuaCheckGroup : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(CommonDataKeys.PROJECT)!!
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file != null) {
            val settings = LuaCheckSettings.getInstance()
            if (settings.valid) {
                runLuaCheck(project, file)
            } else {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, LuaCheckSettingsPanel::class.java)
            }
        }
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        val project = event.getData(CommonDataKeys.PROJECT)
        if (project == null) {
            presentation.isEnabled = false
            presentation.isVisible = false
        } else {
            val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
            presentation.isVisible = file != null && (file.isDirectory || file.fileType == LuaFileType.INSTANCE)
            presentation.isEnabled = true
        }
    }
}