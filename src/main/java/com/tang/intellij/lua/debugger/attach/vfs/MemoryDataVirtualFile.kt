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

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.util.ArrayUtil
import com.intellij.util.LocalTimeCounter
import com.tang.intellij.lua.debugger.attach.CodeState
import com.tang.intellij.lua.lang.LuaFileType
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class MemoryDataVirtualFile(name: String,
                            parent: MemoryVirtualFileDirectory?)
    : MemoryVirtualFile(name, parent) {

    var index: Int = -1
    var state = CodeState.Unavailable

    private var myContents: ByteArray = ArrayUtil.EMPTY_BYTE_ARRAY
    private var myModificationStamp: Long = LocalTimeCounter.currentTime()

    override fun getInputStream(): InputStream {
        return VfsUtilCore.byteStreamSkippingBOM(this.myContents, this)
    }

    override fun getOutputStream(requestor: Any, newModificationStamp: Long, newTimeStamp: Long): OutputStream {
        return VfsUtilCore.outputStreamAddingBOM(object : ByteArrayOutputStream() {
            override fun close() {
                val fs = fileSystem as MemoryFileSystem
                fs.fireBeforeContentsChange(requestor, this@MemoryDataVirtualFile)
                val oldModStamp = myModificationStamp
                myContents = this.toByteArray()
                myModificationStamp = if (newModificationStamp >= 0L) newModificationStamp else LocalTimeCounter.currentTime()
                fs.fireContentsChanged(requestor, this@MemoryDataVirtualFile, oldModStamp)
            }
        }, this)
    }

    override fun getModificationStamp(): Long {
        return myModificationStamp
    }

    override fun contentsToByteArray() = myContents

    override fun getLength(): Long = myContents.size.toLong()

    override fun isDirectory() = false

    override fun isWritable() = false

    override fun getFileType(): FileType {
        return LuaFileType.INSTANCE
    }
}