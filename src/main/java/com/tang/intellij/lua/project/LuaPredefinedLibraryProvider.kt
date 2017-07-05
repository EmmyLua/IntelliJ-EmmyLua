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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor
import com.tang.intellij.lua.psi.LuaFileUtil
import java.io.File

/**
 *
 * Created by Administrator on 2017/7/5.
 */
class LuaPredefinedLibraryProvider : IndexableSetContributor() {

    /*override fun getAdditionalProjectRootsToIndex(project: Project): MutableSet<VirtualFile> {
        val dir = LuaFileUtil.getPluginVirtualFile("std")
        val file = VfsUtil.findFileByIoFile(File(dir), false)
        val list = mutableSetOf<VirtualFile>()
        list.add(file!!)
        return list
    }*/

    override fun getAdditionalRootsToIndex(): Set<VirtualFile> {
        val dir = LuaFileUtil.getPluginVirtualFile("std")
        val file = VfsUtil.findFileByIoFile(File(dir), false)
        val set = mutableSetOf<VirtualFile>()
        set.add(file!!)
        return set
    }
}