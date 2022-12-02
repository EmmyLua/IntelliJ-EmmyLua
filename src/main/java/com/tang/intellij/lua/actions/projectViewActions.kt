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

package com.tang.intellij.lua.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.roots.ModuleRootManager
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.project.LuaSourceRootManager

class MarkLuaSourceRootAction : AnAction(), DumbAware {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (file.isDirectory) {
            ApplicationManager.getApplication().runWriteAction {
                LuaSourceRootManager.getInstance(project).appendRoot(file)
                val module = event.getData(LangDataKeys.MODULE)
                if (module != null) {
                    ModuleRootManager.getInstance(module)?.modifiableModel?.commit()
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = isVisible(e)
        e.presentation.icon = LuaIcons.ROOT
    }

    private fun isVisible(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
        return !LuaSourceRootManager.getInstance(project).isSourceRoot(file)
    }
}

class UnmarkLuaSourceRootAction : AnAction(), DumbAware {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (file.isDirectory) {
            ApplicationManager.getApplication().runWriteAction {
                LuaSourceRootManager.getInstance(project).removeRoot(file)
                val module = event.getData(LangDataKeys.MODULE)
                if (module != null) {
                    ModuleRootManager.getInstance(module)?.modifiableModel?.commit()
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = isVisible(e)
        e.presentation.icon = LuaIcons.ROOT
    }

    private fun isVisible(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
        return LuaSourceRootManager.getInstance(project).isSourceRoot(file)
    }
}