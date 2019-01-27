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

package com.tang.intellij.lua.unity.library

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.vfs.VirtualFile
import com.tang.intellij.lua.project.LuaLibraryRootsComponentDescriptor

class LuaNetLibraryRootsComponentDescriptor : LuaLibraryRootsComponentDescriptor() {

    override fun createAttachFilesChooserDescriptor(libraryName: String?): FileChooserDescriptor {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)

        return object : FileChooserDescriptor(descriptor) {
            override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean {
                if (file.isDirectory)
                    return true

                val ext = file.extension
                return ext != null && ext.equals("dll", ignoreCase = true)
            }

            override fun isFileSelectable(file: VirtualFile): Boolean {
                return super.isFileSelectable(file) || file.extension == "dll"
            }
        }
    }
}