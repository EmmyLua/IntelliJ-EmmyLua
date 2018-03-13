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

package com.tang.intellij.lua.psi.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.TokenSet
import com.tang.intellij.lua.parser.LuaParser
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
        // value expr
        T_VALUE(TokenSet.EMPTY)
    }

    fun parseExpr(builder: PsiBuilder, l:Int): PsiBuilder.Marker? {
        return parseExpr(builder, ExprType.T_OR, l)
    }

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
        ExprType.T_MULTIPLICATIVE -> parseBinary(builder, type.ops, ExprType.T_EXP, l)
        ExprType.T_EXP -> parseBinary(builder, type.ops, ExprType.T_UNARY, l)
        ExprType.T_UNARY -> parseUnary(builder, type.ops, ExprType.T_VALUE, l)
        ExprType.T_VALUE -> parseValue(builder, l)
    }

    private fun parseBinary(builder: PsiBuilder, ops: TokenSet, next: ExprType, l:Int): PsiBuilder.Marker? {
        var result = parseExpr(builder, next, l + 1) ?: return null
        while (true) {
            if (ops.contains(builder.tokenType)) {

                val opMarker = builder.mark()
                builder.advanceLexer()
                opMarker.done(BINARY_OP)

                val right = parseExpr(builder, next, l + 1)
                if (right == null) error(builder, "Expression expected")
                //save
                result = result.precede()
                result.done(BINARY_EXPR)
                if (right == null) break
            } else break
        }
        return result
    }

    private fun parseUnary(b: PsiBuilder, ops: TokenSet, next: ExprType, l: Int): PsiBuilder.Marker? {
        val isUnary = ops.contains(b.tokenType)
        if (isUnary) {
            val m = b.mark()

            val opMarker = b.mark()
            b.advanceLexer()
            opMarker.done(UNARY_OP)

            val right = parseUnary(b, ops, next, l)
            if (right == null) {
                error(b, "Expression expected")
            }
            m.done(UNARY_EXPR)
            return m
        }
        return parseExpr(b, next, l)
    }

    private fun parseValue(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        val pri = parsePrimaryExpr(b, l + 1)
        if (pri != null)
            return pri
        val r = LuaParser.closureExpr(b, l + 1)
        return if (r) b.latestDoneMarker as PsiBuilder.Marker else null
    }

    fun parsePrimaryExpr(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        var prefix = parsePrefixExpr(b, l + 1)
        while (prefix != null) {
            val suffix = parseIndexExpr(prefix, b, l + 1)
                    ?: parseCallExpr(prefix, b, l + 1)
            if (suffix == null) break
            else prefix = suffix
        }
        return prefix
    }

    private fun parseIndexExpr(prefix: PsiBuilder.Marker, b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        when (b.tokenType) {
            DOT, COLON -> { // left indexExpr ::= '[' expr ']' | '.' ID | ':' ID
                b.advanceLexer()
                if (b.tokenType == ID) {
                    b.advanceLexer()
                } else error(b, "ID expected")
                val m = prefix.precede()
                m.done(INDEX_EXPR)
                return m
            }
            LBRACK -> {
                b.advanceLexer()

                val expr = parseExpr(b, l + 1)
                if (expr != null) {
                    if (b.tokenType == RBRACK)
                        b.advanceLexer()
                    else error(b, "']' expected")
                } else error(b, "Expression expected")

                val m = prefix.precede()
                m.done(INDEX_EXPR)
                return m
            }
        }
        return null
    }

    private fun parseCallExpr(prefix: PsiBuilder.Marker, b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        when (b.tokenType) {
            LPAREN -> { // listArgs ::= '(' (arg_expr_list)? ')'
                b.advanceLexer()

                val listArgs = b.mark()
                parseExprList(b, l + 1)
                listArgs.done(LIST_ARGS)

                if (b.tokenType == RPAREN)
                    b.advanceLexer()
                else error(b, "')' expected")

                val m = prefix.precede()
                m.done(CALL_EXPR)
                return m
            }
            STRING -> { // singleArg ::= tableExpr | stringExpr
                val stringExpr = b.mark()
                b.advanceLexer()
                stringExpr.done(LITERAL_EXPR)
                stringExpr.precede().done(SINGLE_ARG)

                val m = prefix.precede()
                m.done(CALL_EXPR)
                return m
            }
            LCURLY -> {
                val tableExpr = parseTableExpr(b, l)
                tableExpr?.precede()?.done(SINGLE_ARG)
                return tableExpr
            }
            else -> return null
        }
    }

    private fun parsePrefixExpr(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        when (b.tokenType) {
            LPAREN -> { // parenExpr ::= '(' expr ')'
                val m = b.mark()
                b.advanceLexer()
                val expr = parseExpr(b, l + 1)
                if (expr != null) {
                    if (b.tokenType == RPAREN) {
                        b.advanceLexer()
                    } else error(b, "')' expected")
                } else error(b, "Expression expected")

                m.done(PAREN_EXPR)
                return m
            }
            ID -> { // nameExpr ::= ID
                val m = b.mark()
                b.advanceLexer()
                m.done(NAME_EXPR)
                return m
            }
            NUMBER, STRING, NIL, TRUE, FALSE, ELLIPSIS -> { //literalExpr ::= nil | false | true | NUMBER | STRING | "..."
                val m = b.mark()
                b.advanceLexer()
                m.done(LITERAL_EXPR)
                return m
            }
            LCURLY -> { // table expr
                return parseTableExpr(b, l)
            }
        }
        return null
    }

    private fun parseTableExpr(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        if (b.tokenType == LCURLY) {
            val m = b.mark()
            b.advanceLexer()

            LuaDeclarationParser.parseTableFieldList(b, l)

            if (b.tokenType == RCURLY) b.advanceLexer()
            else error(b, "'}' expected")

            m.done(TABLE_EXPR)
            return m
        }
        return null
    }

    private fun parseExprList(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        val expr = parseExpr(b, l)
        while (expr != null) {
            if (b.tokenType == COMMA) {
                b.advanceLexer()
                val next = parseExpr(b, l)
                if (next == null) error(b, "Expression expected")
            } else break
        }
        return expr
    }

    private fun error(builder: PsiBuilder, message: String) {
        builder.mark().error(message)
    }
}