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

package com.tang.intellij.lua.unity.vfs

import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.ArchiveFileSystem
import com.intellij.openapi.vfs.newvfs.VfsImplUtil

class NetFileSystem : ArchiveFileSystem() {
    companion object {
        const val PROTOCOL = "dll"
        const val DLL_SEPARATOR = "!/"

        val instance: NetFileSystem
            get() =
            VirtualFileManager.getInstance().getFileSystem(PROTOCOL) as NetFileSystem
    }

    override fun findFileByPathIfCached(path: String): VirtualFile? {
        return VfsImplUtil.findFileByPathIfCached(this, path)
    }

    override fun getProtocol() = PROTOCOL

    override fun extractLocalPath(rootPath: String): String {
        return StringUtil.trimEnd(rootPath, DLL_SEPARATOR)
    }

    override fun findFileByPath(path: String): VirtualFile? {
        return VfsImplUtil.findFileByPath(this, path)
    }

    override fun refreshAndFindFileByPath(path: String): VirtualFile? {
        return VfsImplUtil.refreshAndFindFileByPath(this, path)
    }

    override fun getHandler(entryFile: VirtualFile): NetArchiveHandler {
        return VfsImplUtil.getHandler<NetArchiveHandler>(this, entryFile) { NetArchiveHandler(it) }
    }

    override fun extractRootPath(path: String): String {
        val separatorIndex = path.indexOf(DLL_SEPARATOR)
        assert(separatorIndex >= 0) { "Path passed to JarFileSystem must have jar separator '!/': $path" }
        return path.substring(0, separatorIndex + DLL_SEPARATOR.length)
    }

    override fun refresh(asynchronous: Boolean) {
        VfsImplUtil.refresh(this, asynchronous)
    }

    override fun composeRootPath(localPath: String): String {
        return localPath + DLL_SEPARATOR
    }

    override fun isCorrectFileType(local: VirtualFile): Boolean {
        return super.isCorrectFileType(local) || local.extension == "dll"
    }
}