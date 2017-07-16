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

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx


class LuaCheckGroup : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(DataKeys.PROJECT)!!
        when (event.place) {
            ActionPlaces.EDITOR_POPUP -> {
                val editorManager = FileEditorManager.getInstance(project) as FileEditorManagerEx
                runLuaCheck(project, editorManager.currentFile!!)
            }
        }
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        val project = event.getData(DataKeys.PROJECT)
        if (project == null) {
            presentation.isEnabled = false
            presentation.isVisible = false
        } else {
            presentation.isVisible = true
            presentation.isEnabled = true
            /*if (event.place == "MainMenu") {
                val selectedFiles = FileEditorManager.getInstance(project).selectedFiles
                presentation.isEnabled = selectedFiles.isNotEmpty()
            }*/
        }
    }
}