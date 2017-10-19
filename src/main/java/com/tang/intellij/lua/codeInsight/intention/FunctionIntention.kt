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

package com.tang.intellij.lua.codeInsight.intention

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import com.tang.intellij.lua.psi.LuaClosureExpr
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import com.tang.intellij.lua.psi.LuaPsiTreeUtil

abstract class FunctionIntention : BaseIntentionAction() {
    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val bodyOwner = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaFuncBodyOwner::class.java, false)
        //不对Closure生效
        if (bodyOwner == null || bodyOwner is LuaClosureExpr)
            return false

        //不在body内
        val contains = bodyOwner.funcBody?.textRange?.contains(editor.caretModel.offset)
        if (contains != null && contains)
            return false

        return isAvailable(bodyOwner, editor)
    }

    abstract fun isAvailable(bodyOwner: LuaFuncBodyOwner, editor: Editor): Boolean

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val bodyOwner = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaFuncBodyOwner::class.java, false)
        if (bodyOwner != null) invoke(bodyOwner, editor)
    }

    abstract fun invoke(bodyOwner: LuaFuncBodyOwner, editor: Editor)
}