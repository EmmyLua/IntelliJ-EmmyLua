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

package com.tang.intellij.lua.debugger.attach.vfs

import com.intellij.openapi.vfs.VirtualFile

class MemoryVirtualFileDirectory(name: String, parent: MemoryVirtualFileDirectory?)
    : MemoryVirtualFile(name, parent) {

    private val children = mutableListOf<MemoryVirtualFile>()

    override fun isDirectory() = true
    override fun getLength(): Long = 0

    fun addChild(child: MemoryVirtualFile) {
        children.add(child)
    }

    fun removeChild(child: MemoryVirtualFile) {
        children.remove(child)
        child.myInvalid = false
    }

    override fun getChildren(): Array<VirtualFile> {
        return children.toTypedArray()
    }

    override fun getModificationStamp(): Long {
        return -1
    }
}