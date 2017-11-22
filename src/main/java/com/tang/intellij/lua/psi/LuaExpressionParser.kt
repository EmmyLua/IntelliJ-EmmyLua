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

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase.*
import com.intellij.psi.tree.TokenSet
import com.tang.intellij.lua.psi.LuaTypes.*

object LuaExpressionParser {

    enum class ExprType(val ops: TokenSet) {
        // or
        T_OR(TokenSet.create(OR)),
        // and
        T_AND(TokenSet.create(AND)),
        // < > <= >= ~= ==
        T_CONDITION(TokenSet.create(GT, LT, GE, LE, NE, EQ)),
        // |
        T_BIT_OR(TokenSet.create(BIT_OR)),
        // ~
        T_BIT_TILDE(TokenSet.create(BIT_TILDE)),
        // &
        T_BIT_AND(TokenSet.create(BIT_AND)),
        // << >>
        T_BIT_SHIFT(TokenSet.create(BIT_LTLT, BIT_RTRT)),
        // ..
        T_CONCAT(TokenSet.create(CONCAT)),
        // + -
        T_ADDITIVE(TokenSet.create(PLUS, MINUS)),
        // * / // %
        T_MULTIPLICATIVE(TokenSet.create(MULT, DIV, DOUBLE_DIV, MOD)),
        // not # - ~
        T_UNARY(TokenSet.create(NOT, GETN, MINUS, BIT_TILDE)),
        // ^
        T_EXP(TokenSet.create(EXP)),
    }

    fun parse(builder: PsiBuilder, l:Int)
            = parseExpr(builder, ExprType.T_OR, l)

    private fun parseExpr(builder: PsiBuilder, type: ExprType, l:Int): PsiBuilder.Marker? = when (type) {
        ExprType.T_OR -> parseBinary(builder, type.ops, ExprType.T_AND, l)
        ExprType.T_AND -> parseBinary(builder, type.ops, ExprType.T_CONDITION, l)
        ExprType.T_CONDITION -> parseBinary(builder, type.ops, ExprType.T_BIT_OR, l)
        ExprType.T_BIT_OR -> parseBinary(builder, type.ops, ExprType.T_BIT_TILDE, l)
        ExprType.T_BIT_TILDE -> parseBinary(builder, type.ops, ExprType.T_BIT_AND, l)
        ExprType.T_BIT_AND -> parseBinary(builder, type.ops, ExprType.T_BIT_SHIFT, l)
        ExprType.T_BIT_SHIFT -> parseBinary(builder, type.ops, ExprType.T_CONCAT, l)
        ExprType.T_CONCAT -> parseBinary(builder, type.ops, ExprType.T_ADDITIVE, l)
        ExprType.T_ADDITIVE -> parseBinary(builder, type.ops, ExprType.T_MULTIPLICATIVE, l)
        ExprType.T_MULTIPLICATIVE -> parseBinary(builder, type.ops, ExprType.T_UNARY, l)
        ExprType.T_UNARY -> parseUnary(builder, type.ops, l)
        ExprType.T_EXP -> parseUnary(builder, type.ops, l)
    }

    private fun parseBinary(builder: PsiBuilder, ops: TokenSet, next: ExprType, l:Int): PsiBuilder.Marker? {
        var result = parseExpr(builder, next, l + 1) ?: return null
        while (true) {
            if (ops.contains(builder.tokenType)) {
                builder.advanceLexer()
                val right = parseExpr(builder, next, l + 1)
                //save
                result = result.precede()
                if (right == null) {
                    error(builder, "Expression expected")
                    break
                }
                result.done(BINARY_EXPR)
            } else break
        }
        return result
    }

    private fun parseUnary(b: PsiBuilder, ops: TokenSet, l: Int): PsiBuilder.Marker? {
        var r: Boolean
        val m = enter_section_(b, l, _NONE_, LITERAL_EXPR, "<literal expr>")
        r = consumeToken(b, NIL)
        if (!r) r = consumeToken(b, FALSE)
        if (!r) r = consumeToken(b, TRUE)
        if (!r) r = consumeToken(b, NUMBER)
        if (!r) r = consumeToken(b, STRING)
        if (!r) r = consumeToken(b, ELLIPSIS)
        exit_section_(b, l, m, r, false, null)

        return if (r) m else null
    }

    private fun error(builder: PsiBuilder, message: String) {
        builder.mark().error(message)
    }
}