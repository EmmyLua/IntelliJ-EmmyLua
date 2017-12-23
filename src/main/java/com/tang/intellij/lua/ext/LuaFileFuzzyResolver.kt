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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.ProjectAndLibrariesScope

class LuaFileFuzzyResolver : ILuaFileResolver {
    override fun find(project: Project, shortUrl: String, extNames: Array<String>): VirtualFile? {
        var perfect: PsiFile? = null
        val names = shortUrl.split('/')
        val fileName = names.lastOrNull()
        if (fileName != null) {
            ApplicationManager.getApplication().runReadAction {
                var perfectMatch = Int.MAX_VALUE
                for (extName in extNames) {
                    val files = FilenameIndex.getFilesByName(project, "$fileName$extName", ProjectAndLibrariesScope(project))
                    for (file in files) {
                        val path = file.virtualFile.canonicalPath
                        if (path != null && perfectMatch > path.length && path.endsWith("$shortUrl$extName")) {
                            perfect = file
                            perfectMatch = path.length
                        }
                    }

                    if (perfect != null)
                        break
                }
            }
        }
        return perfect?.virtualFile
    }
}