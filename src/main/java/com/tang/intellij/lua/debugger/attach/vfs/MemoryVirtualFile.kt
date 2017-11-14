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
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.VirtualFileWithId
import com.intellij.openapi.vfs.ex.dummy.DummyFileIdGenerator
import java.io.InputStream
import java.io.OutputStream

abstract class MemoryVirtualFile(private val myName: String,
                                 private val myParent: MemoryVirtualFileDirectory?) : VirtualFile(), VirtualFileWithId {

    private val id = DummyFileIdGenerator.next()
    internal var myInvalid = true

    override fun getId() = id

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) {

    }

    override fun getFileSystem(): VirtualFileSystem = MemoryFileSystem.instance

    override fun getPath(): String {
        val p = parent
        return if (p == null) name else "${p.path}/$name"
    }

    override fun getTimeStamp(): Long {
        return -1
    }

    override fun getName() = myName

    override fun contentsToByteArray(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isValid() = myInvalid

    override fun getInputStream(): InputStream {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParent() = myParent

    override fun getChildren(): Array<VirtualFile>? {
        return null
    }

    override fun isWritable() = true

    override fun getOutputStream(requestor: Any, newModificationStamp: Long, newTimeStamp: Long): OutputStream {
        TODO("not implemented")
    }
}