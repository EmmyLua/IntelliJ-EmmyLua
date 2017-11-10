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

        val instance: MemoryFileSystem get() {
            return VirtualFileManager.getInstance().getFileSystem(PROTOCOL) as MemoryFileSystem
        }
    }

    private var root: VirtualFile? = null

    override fun getProtocol(): String {
        return PROTOCOL
    }

    fun getRoot(): VirtualFile {
        if (root == null) {
            root = createRoot("test")
            refresh(true)
        }
        return root!!
    }

    override fun findFileByPath(path: String): VirtualFile? {
        val list = path.split("/")
        var file = if (list[0] == "test") root else null
        for (i in 1 until list.size) {
            if (file != null) {
                file = file.findChild(list[i])
            }
        }
        return file
    }
}