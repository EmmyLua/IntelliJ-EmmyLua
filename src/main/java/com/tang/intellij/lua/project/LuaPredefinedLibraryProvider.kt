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

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import com.intellij.util.indexing.IndexableSetContributor
import com.intellij.util.io.URLUtil
import com.tang.intellij.lua.psi.LuaFileUtil
import java.io.File

/**
 *
 * Created by Administrator on 2017/7/5.
 */
class LuaPredefinedLibraryProvider : IndexableSetContributor() {

    private val predefined: Set<VirtualFile> by lazy {
        val jarPath = PathUtil.getJarPathForClass(LuaPredefinedLibraryProvider::class.java)
        val dir = if (jarPath.endsWith(".jar")) {
            VfsUtil.findFileByURL(URLUtil.getJarEntryURL(File(jarPath), "std"))
        } else
            VfsUtil.findFileByIoFile(File("$jarPath/std"), true)

        val set = mutableSetOf<VirtualFile>()
        if (dir != null) {
            dir.children.forEach {
                it.putUserData(LuaFileUtil.PREDEFINED_KEY, true)
            }
            set.add(dir)
        }
        set
    }

    override fun getAdditionalRootsToIndex(): Set<VirtualFile> {
        return predefined
    }
}