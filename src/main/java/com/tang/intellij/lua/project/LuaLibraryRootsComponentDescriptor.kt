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

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.ui.AttachRootButtonDescriptor
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor
import com.intellij.openapi.roots.libraries.ui.OrderRootTypePresentation
import com.intellij.openapi.roots.libraries.ui.RootDetector
import com.intellij.openapi.vfs.VirtualFile

/**
 *
 * Created by tangzx on 2016/12/24.
 */
open class LuaLibraryRootsComponentDescriptor : LibraryRootsComponentDescriptor() {
    override fun getRootTypePresentation(orderRootType: OrderRootType): OrderRootTypePresentation? = null

    override fun createAttachFilesChooserDescriptor(libraryName: String?): FileChooserDescriptor {
        return object : FileChooserDescriptor(super.createAttachFilesChooserDescriptor(libraryName)) {
            override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean {
                if (file.isDirectory)
                    return true

                val ext = file.extension
                return ext != null && ext.equals("zip", ignoreCase = true)
            }
        }
    }

    override fun getRootDetectors(): List<RootDetector> =
            listOf(LuaRootDetector(OrderRootType.CLASSES, true, "Lua Sources"))

    override fun createAttachButtons(): List<AttachRootButtonDescriptor> = emptyList()

    internal class LuaRootDetector(rootType: OrderRootType, jarDirectory: Boolean, presentableRootTypeName: String)
        : RootDetector(rootType, jarDirectory, presentableRootTypeName) {

        override fun detectRoots(virtualFile: VirtualFile, progressIndicator: ProgressIndicator) =
                setOf(virtualFile)
    }
}
