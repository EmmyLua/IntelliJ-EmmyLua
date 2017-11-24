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
import com.tang.intellij.lua.psi.*

class ComputeConstantValueIntention : BaseIntentionAction() {
    override fun getFamilyName() = "Compute constant value"

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val expr = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaExpr::class.java, false)
        if (expr is LuaBinaryExpr) {
            val result = ExpressionUtil.compute(expr)
            text = "Compute constant value of ${expr.text}"
            return result != null
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val expr = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaExpr::class.java, false)
        if (expr is LuaBinaryExpr) {
            val result = ExpressionUtil.compute(expr)
            if (result != null) {
                when (result.kind) {
                    ComputeKind.Number,
                    ComputeKind.Bool,
                    ComputeKind.Nil -> {
                        val new = LuaElementFactory.createLiteral(project, result.string)
                        expr.replace(new)
                    }
                    ComputeKind.String -> {
                        val new = LuaElementFactory.createLiteral(project, "[[${result.string}]]")
                        expr.replace(new)
                    }
                    ComputeKind.Other -> {
                        result.expr?.let { expr.replace(it) }
                    }
                }
            }
        }
    }
}