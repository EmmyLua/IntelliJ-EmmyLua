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

package com.tang.intellij.lua.psi

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.SmartList
import com.tang.intellij.lua.ext.ILuaFileResolver
import com.tang.intellij.lua.project.LuaSourceRootManager
import java.io.File

/**
 *
 * Created by tangzx on 2017/1/4.
 */
object LuaFileUtil {

    private val pluginVirtualDirectory: VirtualFile?
        get() {
            val descriptor = PluginManagerCore.getPlugin(PluginId.getId("com.tang"))
            if (descriptor != null) {
                return VirtualFileManager.getInstance().findFileByNioPath(descriptor.pluginPath)
            }

            return null
        }

    var PREDEFINED_KEY = Key.create<Boolean>("lua.lib.predefined")

    fun fileEquals(f1: VirtualFile?, f2: VirtualFile?): Boolean {
        return if (f1 == null || f2 == null) false else f1.path == f2.path
    }

    fun getAllAvailablePathsForMob(shortPath: String?, file: VirtualFile): List<String> {
        val list = SmartList<String>()
        val fullPath = file.canonicalPath
        val extensions = LuaFileManager.extensions
        if (fullPath != null) {
            for (ext in extensions) {
                if (!fullPath.endsWith(ext)) {
                    continue
                }
                list.add(fullPath.substring(0, fullPath.length - ext.length))
            }
        }
        if (shortPath != null) {
            for (ext in extensions) {
                if (!shortPath.endsWith(ext)) {
                    continue
                }
                val path = shortPath.substring(0, shortPath.length - ext.length)
                list.add(path)
                if (path.indexOf('/') != -1)
                    list.add(path.replace('/', '.'))
            }
        }
        return list
    }

    fun findFile(project: Project, shortUrl: String?): VirtualFile? {
        var fixedShortUrl = shortUrl ?: return null

        // "./x.lua" => "x.lua"
        if (fixedShortUrl.startsWith("./") || fixedShortUrl.startsWith(".\\")) {
            fixedShortUrl = fixedShortUrl.substring(2)
        }
        val extensions = LuaFileManager.extensions
        return ILuaFileResolver.findLuaFile(project, fixedShortUrl, extensions)
    }

    fun getShortPath(project: Project, file: VirtualFile): String {
        return VfsUtil.urlToPath(getShortUrl(project, file))
    }

    private fun getShortUrl(project: Project, file: VirtualFile): String {
        val fileFullUrl = file.url
        var fileShortUrl = fileFullUrl

        val roots = LuaSourceRootManager.getInstance(project).getSourceRoots()
        for (root in roots) {
            val sourceRootUrl = root.url
            if (fileFullUrl.startsWith(sourceRootUrl)) {
                fileShortUrl = fileFullUrl.substring(sourceRootUrl.length + 1)
                break
            }
        }
        return fileShortUrl
    }

    private fun getSourceRoot(project: Project, file: VirtualFile): VirtualFile? {
        val fileFullUrl = file.url

        val roots = LuaSourceRootManager.getInstance(project).getSourceRoots()
        for (root in roots) {
            val sourceRootUrl = root.url
            if (fileFullUrl.startsWith(sourceRootUrl)) {
                return root
            }
        }
        return null
    }

    fun getPluginVirtualFile(path: String): String? {
        val directory = pluginVirtualDirectory
        if (directory != null) {
            var fullPath = directory.path + "/classes/" + path
            if (File(fullPath).exists())
                return fullPath
            fullPath = directory.path + "/" + path
            if (File(fullPath).exists())
                return fullPath
        }
        return null
    }

    fun asRequirePath(project: Project, file: VirtualFile): String? {
        val root = getSourceRoot(project, file) ?: return null
        val list = mutableListOf<String>()
        var item = file
        while (item != root) {
            if (item.isDirectory)
                list.add(item.name)
            else
                list.add(FileUtil.getNameWithoutExtension(item.name))
            item = item.parent
        }
        if (list.isEmpty())
            return null
        list.reverse()
        return list.joinToString(".")
    }

    fun isStdLibFile(file: VirtualFile, project: Project): Boolean {
        return file.getUserData(PREDEFINED_KEY) != null || FileIndexFacade.getInstance(project).isInLibraryClasses(file)
    }
}
