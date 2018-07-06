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

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.PathUtil
import com.intellij.util.io.URLUtil
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.lang.LuaLanguageLevel
import com.tang.intellij.lua.psi.LuaFileUtil
import java.io.File
import javax.swing.Icon

class StdLibraryProvider: AdditionalLibraryRootsProvider() {
    override fun getAdditionalProjectLibraries(project: Project): Collection<StdLibrary> {
        val level = LuaSettings.instance.languageLevel
        val std = "std/Lua${level.version}"
        val jarPath = PathUtil.getJarPathForClass(StdLibraryProvider::class.java)
        val dir = if (jarPath.endsWith(".jar"))
            VfsUtil.findFileByURL(URLUtil.getJarEntryURL(File(jarPath), std))
        else
            VfsUtil.findFileByIoFile(File("$jarPath/$std"), true)

        if (dir != null) {
            dir.children.forEach {
                it.putUserData(LuaFileUtil.PREDEFINED_KEY, true)
            }
            return listOf(StdLibrary(level, dir))
        }
        return emptyList()
    }

    companion object {
        fun reload() {
            WriteAction.run<RuntimeException> {
                val projects = ProjectManagerEx.getInstanceEx().openProjects
                for (project in projects) {
                    ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), false, true)
                }

                StubIndex.getInstance().forceRebuild(Throwable("Lua language level changed."))
            }
        }
    }

    class StdLibrary(private val level: LuaLanguageLevel,
                     private val root: VirtualFile) : SyntheticLibrary(), ItemPresentation {
        private val roots = listOf(root)
        override fun hashCode() = root.hashCode()

        override fun equals(other: Any?): Boolean {
            return other is StdLibrary && other.root == root
        }

        override fun getSourceRoots() = roots

        override fun getLocationString() = "Lua std library"

        override fun getIcon(p0: Boolean): Icon = LuaIcons.FILE

        override fun getPresentableText() = level.toString()

    }
}