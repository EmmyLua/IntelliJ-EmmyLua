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

package com.tang.intellij.lua.psi.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.impl.source.tree.FileElement
import com.intellij.testFramework.LightVirtualFile
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaExprCodeFragment
import com.tang.intellij.lua.psi.LuaPsiFile

class LuaExprCodeFragmentImpl(project: Project, name: String, text: CharSequence, var myPhysical: Boolean)
    : LuaPsiFile(PsiManagerEx.getInstanceEx(project).fileManager.createFileViewProvider(LightVirtualFile(name, LuaLanguage.INSTANCE, text), myPhysical)), LuaExprCodeFragment {
    init {
        (viewProvider as SingleRootFileViewProvider).forceCachedPsi(this)
    }

    private var myViewProvider: FileViewProvider? = null
    private var myContext: PsiElement? = null

    fun setContext(context: PsiElement?) {
        myContext = context
    }

    override fun getContext(): PsiElement? {
        val mc = myContext
        if (mc != null && mc.isValid)
            return mc
        return super.getContext()
    }

    override fun clone(): LuaExprCodeFragmentImpl {
        val clone = cloneImpl(calcTreeElement().clone() as FileElement) as LuaExprCodeFragmentImpl
        copyCopyableDataTo(clone)
        clone.myPhysical = false
        clone.myOriginalFile = this
        val fileMgr = (manager as PsiManagerEx).fileManager
        val cloneViewProvider = fileMgr.createFileViewProvider(LightVirtualFile(name, language, text), false) as SingleRootFileViewProvider
        cloneViewProvider.forceCachedPsi(clone)
        clone.myViewProvider = cloneViewProvider
        return clone
    }

    override fun isPhysical(): Boolean {
        return myPhysical
    }

    override fun getViewProvider(): FileViewProvider {
        return myViewProvider ?: super.getViewProvider()
    }
}