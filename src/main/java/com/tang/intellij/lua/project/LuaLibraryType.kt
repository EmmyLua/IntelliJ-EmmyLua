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

package com.tang.intellij.lua.project

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.DummyLibraryProperties
import com.intellij.openapi.roots.libraries.LibraryType
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import com.tang.intellij.lua.lang.LuaIcons
import javax.swing.Icon
import javax.swing.JComponent

/**
 *
 * Created by tangzx on 2016/12/24.
 */
class LuaLibraryType : LibraryType<DummyLibraryProperties>(LuaLibraryKind.INSTANCE) {

    override fun getCreateActionName() = "Lua Zip Library"

    override fun getIcon(properties: DummyLibraryProperties?): Icon = LuaIcons.FILE

    override fun createNewLibrary(parentComponent: JComponent, contextDirectory: VirtualFile?, project: Project): NewLibraryConfiguration? {
        val chooser = createLibraryRootsComponentDescriptor()
        val files = FileChooser.chooseFiles(chooser.createAttachFilesChooserDescriptor("lua"), project, contextDirectory)

        return object : NewLibraryConfiguration(suggestLibraryName(files), this, DummyLibraryProperties()) {

            override fun addRoots(libraryEditor: LibraryEditor) {
                for (file in files) {
                    libraryEditor.addRoot(file, OrderRootType.CLASSES)
                }
            }
        }
    }

    override fun createLibraryRootsComponentDescriptor() =
            LuaLibraryRootsComponentDescriptor()

    override fun createPropertiesEditor(libraryEditorComponent: LibraryEditorComponent<DummyLibraryProperties>): LibraryPropertiesEditor? =
            null

    companion object {

        fun instance() = LibraryType.findByKind(LuaLibraryKind.INSTANCE) as LuaLibraryType

        private fun suggestLibraryName(classesRoots: Array<VirtualFile>): String =
                if (classesRoots.isNotEmpty()) FileUtil.getNameWithoutExtension(PathUtil.getFileName(classesRoots[0].path)) else "Unnamed"
    }
}
