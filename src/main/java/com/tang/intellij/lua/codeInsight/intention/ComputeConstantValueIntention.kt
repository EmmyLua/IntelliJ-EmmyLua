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
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.LuaTypes.*

class ComputeConstantValueIntention : BaseIntentionAction() {
    override fun getFamilyName() = "Compute constant value"

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val expr = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaExpr::class.java, false)
        if (expr is LuaBinaryExpr) {
            val result = compute(expr)
            text = "Compute constant value of ${expr.text}"
            return result != null
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val expr = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaExpr::class.java, false)
        if (expr is LuaBinaryExpr) {
            val result = compute(expr)
            if (result != null) {
                when (result.kind) {
                    Kind.Number,
                    Kind.Bool,
                    Kind.Nil -> {
                        val new = LuaElementFactory.createLiteral(project, result.string)
                        expr.replace(new)
                    }
                    Kind.String -> {
                        val new = LuaElementFactory.createLiteral(project, "[[${result.string}]]")
                        expr.replace(new)
                    }
                    Kind.Other -> {
                        result.expr?.let { expr.replace(it) }
                    }
                }
            }
        }
    }

    private data class ComputeResult(val kind: Kind,
                                     var bValue: Boolean = false,
                                     var nValue: Float = 0f,
                                     var sValue: String = "",
                                     var expr: LuaExpr? = null) {
        val string: String get() = when (kind) {
            Kind.Number -> {
                val int = nValue.toInt()
                if (int.compareTo(nValue) == 0) int.toString() else nValue.toString()
            }
            Kind.String -> sValue
            Kind.Bool -> bValue.toString()
            else -> sValue
        }
    }

    private enum class Kind {
        String, Bool, Number, Nil, Other
    }

    private fun compute(expr: LuaExpr): ComputeResult? {
        return when (expr) {
            is LuaLiteralExpr -> {
                when (expr.kind) {
                    LuaLiteralKind.String -> ComputeResult(Kind.String, false, 0f, expr.stringValue)
                    LuaLiteralKind.Bool -> ComputeResult(Kind.Bool, expr.boolValue)
                    LuaLiteralKind.Number -> ComputeResult(Kind.Number, false, expr.numberValue)
                    LuaLiteralKind.Nil -> ComputeResult(Kind.Nil, false, 0f, "nil")
                    else -> null
                }
            }
            is LuaBinaryExpr -> {
                val left = compute(expr.left) ?: return null
                val rExpr = expr.right ?: return null
                val right = compute(rExpr) ?: return null
                val op = expr.binaryOp
                return calcBinary(left, right, op.node.firstChildNode.elementType)
            }
            is LuaParenExpr -> {
                val inner = expr.expr
                if (inner != null) compute(inner) else null
            }
            else -> return ComputeResult(Kind.Other, true, 0f, "", expr)
        }
    }

    private fun calcBinary(l: ComputeResult, r: ComputeResult, op: IElementType): ComputeResult? {
        var b = false
        var n = 0f
        var s = ""
        var k = l.kind
        var isValid = false

        when (op) {
            OR -> {
                return if (l.bValue) l else r
            }
            AND -> {
                return if (l.bValue) r else l
            }
            // +
            PLUS -> {
                n = l.nValue + r.nValue
                isValid = l.kind == Kind.Number && r.kind == Kind.Number
            }
            // -
            MINUS -> {
                n = l.nValue - r.nValue
                isValid = l.kind == Kind.Number && r.kind == Kind.Number
            }
            // *
            MULT -> {
                n = l.nValue * r.nValue
                isValid = l.kind == Kind.Number && r.kind == Kind.Number
            }
            // /
            DIV -> {
                n = l.nValue / r.nValue
                isValid = l.kind == Kind.Number && r.kind == Kind.Number
                isValid = isValid && r.nValue != 0f
            }
            // //
            DOUBLE_DIV -> {
                n = l.nValue / r.nValue
                n = n.toInt().toFloat()
                isValid = l.kind == Kind.Number && r.kind == Kind.Number
            }
            // %
            MOD -> {
                n = l.nValue % r.nValue
                isValid = l.kind == Kind.Number && r.kind == Kind.Number
            }
            // ..
            CONCAT -> {
                if (l.kind == Kind.String) {
                    isValid = r.kind == Kind.String || r.kind == Kind.Number
                } else if (r.kind == Kind.String) {
                    isValid = l.kind == Kind.Number
                }
                k = Kind.String
                if (isValid)
                    s = l.string + r.string
            }
        }

        b = b || when (k) {
            Kind.Other,
            Kind.Number,
            Kind.String -> true
            else -> false
        }

        return if (isValid) ComputeResult(k, b, n, s) else null
    }
}