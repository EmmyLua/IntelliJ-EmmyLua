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

import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.EditorTabbedContainer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.vcs.FileStatusManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.problems.WolfTheProblemSolver
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.util.IconUtil
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel

class MemoryFilesPanel(val project: Project) : JPanel(BorderLayout()) {

    internal inner class VirtualFilesRenderer : ColoredListCellRenderer<VirtualFile>() {
        override fun customizeCellRenderer(p0: JList<out VirtualFile>, virtualFile: VirtualFile, index: Int, selected: Boolean, hasFocus: Boolean) {
            icon = IconUtil.getIcon(virtualFile, Iconable.ICON_FLAG_READ_STATUS, project)
            val renderedName = EditorTabbedContainer.calcFileName(project, virtualFile)
            val fileStatus = FileStatusManager.getInstance(project).getStatus(virtualFile)

            val hasProblem = WolfTheProblemSolver.getInstance(project).isProblemFile(virtualFile)
            val attributes = TextAttributes(fileStatus.color, null, if (hasProblem) JBColor.red else null, EffectType.WAVE_UNDERSCORE, Font.PLAIN)
            append(renderedName, SimpleTextAttributes.fromTextAttributes(attributes))

            // calc color the same way editor tabs do this, i.e. including EPs
            val color = EditorTabbedContainer.calcTabColor(project, virtualFile)

            if (!selected && color != null) {
                background = color
            }
        }
    }

    private val model = DefaultListModel<VirtualFile>()
    private val list = JBList<VirtualFile>(model)

    init {
        list.cellRenderer = VirtualFilesRenderer()
        list.addListSelectionListener { evt ->
            val file = model.get(evt.firstIndex)
            val isOpen = FileEditorManager.getInstance(project).isFileOpen(file)
            if (!isOpen)
                FileEditorManager.getInstance(project).openFile(file, true)
        }
        add(ScrollPaneFactory.createScrollPane(list), BorderLayout.CENTER)
    }

    fun addFile(virtualFile: VirtualFile) {
        model.addElement(virtualFile)
    }
}