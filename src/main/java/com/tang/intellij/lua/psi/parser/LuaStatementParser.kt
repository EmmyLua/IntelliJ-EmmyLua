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
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.psi.LuaParserUtil
import com.tang.intellij.lua.psi.LuaTypes.*

object LuaStatementParser : GeneratedParserUtilBase() {
    fun parseStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        return when (b.tokenType) {
            DO -> parseDoStatement(b, l)
            IF -> parseIfStatement(b, l)
            WHILE -> parseWhileStatement(b, l)
            REPEAT -> parseRepeatStatement(b, l)
            DOUBLE_COLON -> parseLabelStatement(b)
            GOTO -> parseGotoStatement(b)
            BREAK -> parseBreakStatement(b)
            RETURN -> parseReturnStatement(b, l)
            LOCAL -> parseLocalDef(b, l)
            FOR -> parseForStatement(b, l)
            FUNCTION -> parseFunctionStatement(b, l)
            SEMI -> parseEmptyStatement(b)
            else -> parseOtherStatement(b, l)
        }
    }

    private fun parseDoStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // do

        // block
        LuaParserUtil.lazyBlock(b, l)

        expectError(b, END) { "'end'" } // end

        doneStat(b, m, DO_STAT)
        return m
    }

    // 'if' expr 'then' <<lazyBlock>> ('elseif' expr 'then' <<lazyBlock>>)* ('else' <<lazyBlock>>)? 'end'
    private fun parseIfStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        val m = b.mark()
        b.advanceLexer() // if

        // expr
        expectExpr(b, l + 1)

        // then
        expectError(b, THEN) { "'then'" }

        // block
        LuaParserUtil.lazyBlock(b, l)

        // elseif
        while (b.tokenType == ELSEIF) {
            b.advanceLexer()
            expectExpr(b, l + 1)
            expectError(b, THEN) { "'then'" }
            LuaParserUtil.lazyBlock(b, l)
        }

        // else
        if (b.tokenType == ELSE) {
            b.advanceLexer()
            LuaParserUtil.lazyBlock(b, l)
        }

        expectError(b, END) { "'end'" }
        doneStat(b, m, IF_STAT)
        return m
    }

    // 'while' expr 'do' <<lazyBlock>> 'end'
    private fun parseWhileStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // while

        expectExpr(b, l + 1) // expr

        expectError(b, DO) { "'do'" }

        // block
        LuaParserUtil.lazyBlock(b, l)

        expectError(b, END) { "'end'" } // end

        doneStat(b, m, WHILE_STAT)
        return m
    }

    // 'repeat' <<lazyBlock>> 'until' expr
    private fun parseRepeatStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // repeat

        // block
        LuaParserUtil.lazyBlock(b, l)

        expectError(b, UNTIL) { "'until'" }

        expectExpr(b, l + 1) // expr

        doneStat(b, m, REPEAT_STAT)
        return m
    }

    // '::' ID '::'
    private fun parseLabelStatement(b: PsiBuilder): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // ::

        expectError(b, ID) { "ID" }

        expectError(b, DOUBLE_COLON) { "'::'" } // ::

        doneStat(b, m, LABEL_STAT)
        return m
    }

    // forAStat ::= 'for' paramNameDef '=' expr ',' expr (',' expr)? 'do' <<lazyBlock>> 'end'
    // forBStat ::= 'for' parList 'in' exprList 'do' <<lazyBlock>> 'end'
    private fun parseForStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // for

        val nameCount = expectParamNameOrList(b)
        val type = if (nameCount > 1 || b.tokenType == IN) { // forBStat
            expectError(b, IN) { "'in'" }
            expectExprList(b, l)

            FOR_B_STAT
        } else { // forAStat
            expectError(b, ASSIGN) { "'='" }

            expectExpr(b, l + 1) // expr
            expectError(b, COMMA) { "','" }
            expectExpr(b, l + 1) // expr

            if (b.tokenType == COMMA) {
                b.advanceLexer() // ,
                expectExpr(b, l + 1) // expr
            }

            FOR_A_STAT
        }

        expectError(b, DO) { "'do'" } // do
        LuaParserUtil.lazyBlock(b, l) // block
        expectError(b, END) { "'end'" } // do

        doneStat(b, m, type)
        return m
    }

    private fun expectParamName(b: PsiBuilder, error: Boolean = true): PsiBuilder.Marker? {
        if (b.tokenType == ID) {
            val m = b.mark()
            b.advanceLexer()
            m.done(PARAM_NAME_DEF)
            return m
        } else if (error) b.error("ID expected")
        return null
    }

    private fun expectParamNameOrList(b: PsiBuilder): Int {
        var nameCount = 0
        val firstName = expectParamName(b)
        if (firstName != null) {
            nameCount++
            while (b.tokenType == COMMA) {
                b.advanceLexer()
                expectParamName(b)
                nameCount++
            }
        }
        return nameCount
    }

    private fun parseBreakStatement(b: PsiBuilder): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer()

        doneStat(b, m, BREAK_STAT)
        return m
    }

    private fun parseReturnStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer()

        expectExprList(b, l, false)

        doneStat(b, m, RETURN_STAT)
        return m
    }

    private fun parseLocalDef(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        return if (b.lookAhead(1) == FUNCTION)
            parseLocalFunction(b, l)
        else
            parseLocal(b, l)
    }

    private fun parseLocalFunction(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() //local
        b.advanceLexer() //function

        expectError(b, ID) { "ID" }

        parseFuncBody(b, l)

        doneStat(b, m, LOCAL_FUNC_DEF)
        return m
    }

    private fun parseNameList(b: PsiBuilder): PsiBuilder.Marker? {
        var m = b.mark()
        if (expectError(b, ID) { "ID" }) {
            m.done(NAME_DEF)
            while (b.tokenType == COMMA) {
                b.advanceLexer()
                val nameDef = b.mark()
                if (expectError(b, ID) { "ID" })
                    nameDef.done(NAME_DEF)
                else nameDef.drop()
            }
            m = m.precede()
        }

        m.done(NAME_LIST)
        return m
    }

    private fun parseLocal(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // local

        parseNameList(b)

        if (b.tokenType == ASSIGN) {
            b.advanceLexer()

            val exprList = b.mark()
            if (LuaExpressionParser.parseExprList(b, l + 1) == null)
                b.error("Expression expected")
            exprList.done(EXPR_LIST)
        }

        doneStat(b, m, LOCAL_DEF)
        return m
    }

    fun parseFuncBody(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        expectError(b, LPAREN) { "'('" }

        // param list
        val def = expectParamName(b, false)
        while (def != null) {
            if (expect(b, COMMA)) {
                if (expectParamName(b, false) == null && !expect(b, ELLIPSIS)) {
                    b.error("ID or '...' expected")
                }
            } else break
        }
        // (...)
        if (def == null) {
            expect(b, ELLIPSIS)
        }

        expectError(b, RPAREN) { "')'" }

        // block
        LuaParserUtil.lazyBlock(b, l)

        expectError(b, END) { "'end'" }

        m.done(FUNC_BODY)
        return m
    }

    private fun parseGotoStatement(b: PsiBuilder): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer()
        expectError(b, ID) { "ID" }
        doneStat(b, m, GOTO_STAT)
        return m
    }

    private fun parseFunctionStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // function
        var type = FUNC_DEF

        if (b.tokenType == ID) {
            val ahead = b.lookAhead(1)
            if (ahead != DOT && ahead != COLON) {
                type = FUNC_DEF
                b.advanceLexer() // ID
            } else {
                type = CLASS_METHOD_DEF
                // name expr
                val nameExpr = b.mark()
                b.advanceLexer()
                nameExpr.done(NAME_EXPR)

                var c = nameExpr
                // .ID
                while (b.tokenType == DOT || b.tokenType == COLON) {
                    b.advanceLexer()
                    expectError(b, ID) { "ID" }
                    val next = b.tokenType
                    if (next == DOT || next == COLON) {
                        c = c.precede()
                        c.done(INDEX_EXPR)
                    } else break
                }
                // .ID | :ID
                c = c.precede()
                c.done(CLASS_METHOD_NAME)
            }
        } else b.error("ID expected")

        parseFuncBody(b, l + 1)
        doneStat(b, m, type)
        return m
    }

    private fun parseOtherStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        val expr = LuaExpressionParser.parseExpr(b, l + 1)
        if (expr != null) {
            // check ass
            val last = b.latestDoneMarker
            when (last?.tokenType) {
                NAME_EXPR, INDEX_EXPR -> {
                    var c = expr
                    var isAssignment = false
                    while (b.tokenType == COMMA) {
                        isAssignment = true
                        b.advanceLexer() // ,
                        expectExpr(b, l + 1)
                    }

                    // =
                    if (isAssignment) {
                        c = c.precede()
                        c.done(VAR_LIST)
                        expectError(b, ASSIGN) { "'='" }
                    } else if (b.tokenType == ASSIGN) {
                        c = c.precede()
                        c.done(VAR_LIST)

                        b.advanceLexer()
                        isAssignment = true
                    }

                    if (isAssignment) {
                        expectExprList(b, l + 1)
                        val m = c.precede()
                        doneStat(b, m, ASSIGN_STAT)
                        return m
                    }
                }
            }

            val m = expr.precede()
            doneStat(b, m, EXPR_STAT)
            return m
        }
        return null
    }

    private fun parseEmptyStatement(b: PsiBuilder): PsiBuilder.Marker? {
        val m = b.mark()
        while (b.tokenType == SEMI) {
            b.advanceLexer() // ;
        }
        done(m, EMPTY_STAT)
        return m
    }

    private fun doneStat(b:PsiBuilder, m: PsiBuilder.Marker, type: IElementType) {
        expect(b, SEMI)
        done(m, type)
    }
}