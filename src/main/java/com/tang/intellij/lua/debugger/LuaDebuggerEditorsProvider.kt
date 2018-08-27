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

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase
import com.tang.intellij.lua.lang.LuaFileType
import com.tang.intellij.lua.psi.impl.LuaExprCodeFragmentImpl

/**
 *
 * Created by TangZX on 2016/12/30.
 */
class LuaDebuggerEditorsProvider : XDebuggerEditorsProviderBase() {
    override fun createExpressionCodeFragment(project: Project, text: String, context: PsiElement?, isPhysical: Boolean): PsiFile {
        val fragment = LuaExprCodeFragmentImpl(project,"fragment.lua", text, isPhysical)
        fragment.context = context
        return fragment
    }

    override fun getFileType(): FileType {
        return LuaFileType.INSTANCE
    }
}