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

package com.tang.intellij.lua.ext

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.tang.intellij.lua.project.LuaSettings

class LuaFileAdditionalResolver : ILuaFileResolver {
    override fun find(project: Project, shortUrl: String, extNames: Array<String>): VirtualFile? {
        val sourcesRoot = LuaSettings.instance.additionalSourcesRoot
        var firstMatch: VirtualFile? = null
        for (sr in sourcesRoot) {
            for (ext in extNames) {
                val path = "$sr/$shortUrl$ext"
                val file = VirtualFileManager.getInstance().findFileByUrl(VfsUtil.pathToUrl(path))
                if (file != null && !file.isDirectory) {
                    firstMatch = firstMatch ?: file
                    val filePath = file.canonicalPath
                    val projectBasePath = project.basePath
                    // Is the file in the project folder?
                    if(filePath != null && projectBasePath != null && filePath.startsWith(projectBasePath))
                        return file
                }
            }
        }
        // If no file is found or found files do not in project folder, return the first match (maybe bull)
        // the same as previous behaviour
        return firstMatch
    }
}