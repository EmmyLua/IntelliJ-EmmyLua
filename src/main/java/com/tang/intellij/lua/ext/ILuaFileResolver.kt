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

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface ILuaFileResolver {
    companion object {
        private val EP_NAME = ExtensionPointName.create<ILuaFileResolver>("com.tang.intellij.lua.luaFileResolver")

        fun findLuaFile(project: Project, shortUrl: String, extNames: Array<String>): VirtualFile? {
            for (resolver in EP_NAME.extensions) {
                val file = resolver.find(project, shortUrl, extNames)
                if (file != null)
                    return file
            }
            return null
        }
    }

    fun find(project: Project, shortUrl: String, extNames: Array<String>): VirtualFile?
}