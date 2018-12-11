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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.tang.intellij.lua.project.LuaSourceRootManager

class LuaFileSourcesRootResolver : ILuaFileResolver {
    override fun find(project: Project, shortUrl: String, extNames: Array<String>): VirtualFile? {
        for (sourceRoot in LuaSourceRootManager.getInstance(project).getSourceRootUrls()) {
            val file = findFile(shortUrl, sourceRoot, extNames)
            if (file != null) return file
        }
        return null
    }

    private fun findFile(shortUrl: String, root: String, extensions: Array<String>): VirtualFile? {
        for (ext in extensions) {
            var fixedURL = shortUrl
            if (shortUrl.endsWith(ext)) { //aa.bb.lua -> aa.bb
                fixedURL = shortUrl.substring(0, shortUrl.length - ext.length)
            }

            //将.转为/，但不处理 ..
            if (!fixedURL.contains("/")) {
                //aa.bb -> aa/bb
                fixedURL = fixedURL.replace("\\.".toRegex(), "/")
            }

            fixedURL += ext

            val file = VirtualFileManager.getInstance().findFileByUrl("$root/$fixedURL")
            if (file != null && !file.isDirectory) {
                return file
            }
        }
        return null
    }
}