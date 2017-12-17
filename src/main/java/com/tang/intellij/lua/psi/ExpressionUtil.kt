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

package com.tang.intellij.lua.psi

import com.intellij.psi.tree.IElementType

data class ComputeResult(val kind: ComputeKind,
                         var bValue: Boolean = false,
                         var nValue: Float = 0f,
                         var sValue: String = "",
                         var expr: LuaExpr? = null) {
    val string: String get() = when (kind) {
        ComputeKind.Number -> {
            val int = nValue.toInt()
            if (int.compareTo(nValue) == 0) int.toString() else nValue.toString()
        }
        ComputeKind.String -> sValue
        ComputeKind.Bool -> bValue.toString()
        else -> sValue
    }
}

enum class ComputeKind {
    String, Bool, Number, Nil, Other
}

class ExpressionUtil {
    companion object {

        fun compute(expr: LuaExpr): ComputeResult? {
            return when (expr) {
                is LuaLiteralExpr -> {
                    when (expr.kind) {
                        LuaLiteralKind.String -> ComputeResult(ComputeKind.String, false, 0f, expr.stringValue)
                        LuaLiteralKind.Bool -> ComputeResult(ComputeKind.Bool, expr.boolValue)
                        LuaLiteralKind.Number -> ComputeResult(ComputeKind.Number, false, expr.numberValue)
                        LuaLiteralKind.Nil -> ComputeResult(ComputeKind.Nil, false, 0f, "nil")
                        else -> null
                    }
                }
                is LuaBinaryExpr -> {
                    val left = compute(expr.left!!) ?: return null
                    val rExpr = expr.right ?: return null
                    val right = compute(rExpr) ?: return null
                    val op = expr.binaryOp
                    return calcBinary(left, right, op.node.firstChildNode.elementType)
                }
                is LuaParenExpr -> {
                    val inner = expr.expr
                    if (inner != null) compute(inner) else null
                }
                else -> return ComputeResult(ComputeKind.Other, true, 0f, "", expr)
            }
        }

        private fun calcBinary(l: ComputeResult, r: ComputeResult, op: IElementType): ComputeResult? {
            var b = false
            var n = 0f
            var s = ""
            var k = l.kind
            var isValid = false

            when (op) {
                LuaTypes.OR -> {
                    return if (l.bValue) l else r
                }
                LuaTypes.AND -> {
                    return if (l.bValue) r else l
                }
                // +
                LuaTypes.PLUS -> {
                    n = l.nValue + r.nValue
                    isValid = l.kind == ComputeKind.Number && r.kind == ComputeKind.Number
                }
                // -
                LuaTypes.MINUS -> {
                    n = l.nValue - r.nValue
                    isValid = l.kind == ComputeKind.Number && r.kind == ComputeKind.Number
                }
                // *
                LuaTypes.MULT -> {
                    n = l.nValue * r.nValue
                    isValid = l.kind == ComputeKind.Number && r.kind == ComputeKind.Number
                }
                // /
                LuaTypes.DIV -> {
                    n = l.nValue / r.nValue
                    isValid = l.kind == ComputeKind.Number && r.kind == ComputeKind.Number
                    isValid = isValid && r.nValue != 0f
                }
                // //
                LuaTypes.DOUBLE_DIV -> {
                    n = l.nValue / r.nValue
                    n = n.toInt().toFloat()
                    isValid = l.kind == ComputeKind.Number && r.kind == ComputeKind.Number
                }
                // %
                LuaTypes.MOD -> {
                    n = l.nValue % r.nValue
                    isValid = l.kind == ComputeKind.Number && r.kind == ComputeKind.Number
                }
                // ..
                LuaTypes.CONCAT -> {
                    if (l.kind == ComputeKind.String) {
                        isValid = r.kind == ComputeKind.String || r.kind == ComputeKind.Number
                    } else if (r.kind == ComputeKind.String) {
                        isValid = l.kind == ComputeKind.Number
                    }
                    k = ComputeKind.String
                    if (isValid)
                        s = l.string + r.string
                }
            }

            b = b || when (k) {
                ComputeKind.Other,
                ComputeKind.Number,
                ComputeKind.String -> true
                else -> false
            }

            return if (isValid) ComputeResult(k, b, n, s) else null
        }
    }
}