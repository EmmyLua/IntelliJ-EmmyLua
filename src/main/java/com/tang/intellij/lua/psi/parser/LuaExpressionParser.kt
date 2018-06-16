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
import com.tang.intellij.lua.psi.LuaParserUtil.MY_LEFT_COMMENT_BINDER
import com.tang.intellij.lua.psi.LuaParserUtil.MY_RIGHT_COMMENT_BINDER
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
        return parseClosureExpr(b, l + 1)
    }

    private fun parsePrimaryExpr(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        var prefix = parsePrefixExpr(b, l + 1)
        while (prefix != null) {
            val suffix = parseIndexExpr(prefix, b, l + 1)
                    ?: parseCallExpr(prefix, b, l + 1)
            if (suffix == null) break
            else prefix = suffix
        }
        return prefix
    }

    private fun parseClosureExpr(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        if (b.tokenType == FUNCTION) {
            val m = b.mark()
            b.advanceLexer()

            LuaStatementParser.parseFuncBody(b, l + 1)

            m.done(CLOSURE_EXPR)
            return m
        }
        return null
    }

    private fun parseIndexExpr(prefix: PsiBuilder.Marker, b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        when (b.tokenType) {
            DOT, COLON -> { // left indexExpr ::= '[' expr ']' | '.' ID | ':' ID
                b.advanceLexer() // . or :
                expectError(b, ID) { "ID" }
                val m = prefix.precede()
                m.done(INDEX_EXPR)
                return m
            }
            LBRACK -> {
                b.advanceLexer() // [

                expectExpr(b, l + 1) // expr

                expectError(b, RBRACK) { "']'" } // ]

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

                val listArgs = b.mark()
                b.advanceLexer() // (
                parseExprList(b, l + 1)
                expectError(b, RPAREN) { "')'" }
                listArgs.done(LIST_ARGS)

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

                val m = prefix.precede()
                m.done(CALL_EXPR)
                return m
            }
            else -> return null
        }
    }

    private fun parsePrefixExpr(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        when (b.tokenType) {
            LPAREN -> { // parenExpr ::= '(' expr ')'
                val m = b.mark()
                b.advanceLexer() // (

                expectExpr(b, l + 1) // expr

                expectError(b, RPAREN) { "')'" } // )

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

            parseTableFieldList(b, l)

            expectError(b, RCURLY) { "'}'" }

            m.done(TABLE_EXPR)
            return m
        }
        return null
    }

    private fun parseTableFieldList(b: PsiBuilder, l: Int): Boolean {
        val result = parseTableField(b, l)
        while (result != null) {
            val sep = parseTableSep(b)
            val sepError = if (sep == null) b.mark() else null
            sepError?.error(", or ; expected")
            val nextField = parseTableField(b, l)
            if (nextField == null)
                sepError?.drop()
            nextField ?: break
        }
        return true
    }

    private fun parseTableSep(b: PsiBuilder): PsiBuilder.Marker? {
        when (b.tokenType) {
            SEMI, COMMA -> {
                val mark = b.mark()
                b.advanceLexer()
                mark.done(TABLE_FIELD_SEP)
                return mark
            }
        }
        return null
    }

    private fun parseTableField(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        when (b.tokenType) {
            LBRACK -> { // '[' expr ']' '=' expr
                val m = b.mark()
                b.advanceLexer()

                expectExpr(b, l + 1) // expr

                expectError(b, RBRACK) { "']'" }

                expectError(b, ASSIGN) { "'='" }

                expectExpr(b, l + 1) // expr

                m.done(TABLE_FIELD)
                m.setCustomEdgeTokenBinders(MY_LEFT_COMMENT_BINDER, MY_RIGHT_COMMENT_BINDER)
                return m
            }
            ID -> { // ID '=' expr
                val m = b.mark()
                b.advanceLexer()
                if (b.tokenType == ASSIGN) {
                    b.advanceLexer() // =
                    expectExpr(b, l + 1) // expr
                    m.done(TABLE_FIELD)
                    m.setCustomEdgeTokenBinders(MY_LEFT_COMMENT_BINDER, MY_RIGHT_COMMENT_BINDER)
                    return m
                }
                m.rollbackTo()
            }
        }

        // expr
        val expr = LuaExpressionParser.parseExpr(b, l + 1)
        if (expr != null) {
            val m = expr.precede()
            m.done(TABLE_FIELD)
            m.setCustomEdgeTokenBinders(MY_LEFT_COMMENT_BINDER, MY_RIGHT_COMMENT_BINDER)
            return m
        }
        return null
    }

    fun parseExprList(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        val expr = parseExpr(b, l)
        while (expr != null) {
            if (b.tokenType == COMMA) {
                b.advanceLexer() // ,
                expectExpr(b, l + 1) // expr
            } else break
        }
        return expr
    }

    private fun error(builder: PsiBuilder, message: String) {
        builder.mark().error(message)
    }
}