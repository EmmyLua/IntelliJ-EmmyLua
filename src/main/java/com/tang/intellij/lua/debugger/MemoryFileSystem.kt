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

package com.tang.intellij.lua.debugger

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.ex.dummy.DummyFileSystem

class MemoryFileSystem : DummyFileSystem() {

    companion object {
        val PROTOCOL = "lua-dummy"
        private val ROOT = "root"

        val instance: MemoryFileSystem get() {
            return VirtualFileManager.getInstance().getFileSystem(PROTOCOL) as MemoryFileSystem
        }
    }

    private var root: VirtualFile? = null

    fun clear() {
        root?.let { r ->
            r.children.forEach {
                deleteFile(null, it)
            }
        }
    }

    override fun getProtocol(): String {
        return PROTOCOL
    }

    fun getRoot(): VirtualFile {
        if (root == null) {
            root = createRoot(ROOT)
            refresh(true)
        }
        return root!!
    }

    fun findMemoryFile(path: String): VirtualFile? {
        val list = path.split("/")
        var file = root
        for (i in 0 until list.size) {
            if (file != null) {
                file = file.findChild(list[i])
            }
        }
        return file
    }

    override fun findFileByPath(path: String): VirtualFile? {
        val list = path.split("/")
        var file = if (list[0] == ROOT) root else null
        for (i in 1 until list.size) {
            if (file != null) {
                file = file.findChild(list[i])
            }
        }
        return file
    }
}