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
import com.tang.intellij.lua.parser.LuaParser
import com.tang.intellij.lua.psi.LuaParserUtil
import com.tang.intellij.lua.psi.LuaTypes.*

object LuaStatementParser : GeneratedParserUtilBase() {
    fun parseStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        return when (b.tokenType) {
            DO -> parseDoStatement(b, l)
            IF -> parseIfStatement(b, l)
            WHILE -> parseWhileStatement(b, l)
            REPEAT -> parseRepeatStatement(b, l)
            DOUBLE_COLON -> parseLabelStatement(b, l)
            GOTO -> parseGotoStatement(b, l)
            BREAK -> parseBreakStatement(b, l)
            RETURN -> parseReturnStatement(b, l)
            LOCAL -> {
                if (b.lookAhead(1) == FUNCTION)
                    parseLocalFunction(b, l)
                else
                    parseLocal(b, l)
            }
            FOR -> parseForStatement(b, l)
            else -> null
        }
    }

    private fun parseDoStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // do

        // block
        LuaParserUtil.lazyBlock(b, l)

        expectError(b, END) { "'end'" } // end

        m.done(DO_STAT)
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
        m.done(IF_STAT)
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

        m.done(WHILE_STAT)
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

        m.done(REPEAT_STAT)
        return m
    }

    // '::' ID '::'
    private fun parseLabelStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // ::

        expectError(b, ID) { "ID" }

        expectError(b, DOUBLE_COLON) { "'::'" } // ::

        m.done(LABEL_STAT)
        return m
    }

    // forAStat ::= 'for' paramNameDef '=' expr ',' expr (',' expr)? 'do' <<lazyBlock>> 'end'
    // forBStat ::= 'for' parList 'in' exprList 'do' <<lazyBlock>> 'end'
    private fun parseForStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // for

        val nameCount = expectParamNameOrList(b, l)
        val type = if (nameCount > 1) { // forBStat
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

        m.done(type)
        return m
    }

    private fun expectParamName(b: PsiBuilder): PsiBuilder.Marker? {
        if (b.tokenType == ID) {
            val m = b.mark()
            b.advanceLexer()
            m.done(PARAM_NAME_DEF)
            return m
        } else b.error("ID expected")
        return null
    }

    private fun expectParamNameOrList(b: PsiBuilder, l: Int): Int {
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

    private fun parseBreakStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer()

        m.done(BREAK_STAT)
        return m
    }

    private fun parseReturnStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer()

        expectExprList(b, l, false)

        m.done(RETURN_STAT)
        return m
    }

    private fun parseLocalFunction(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() //local
        b.advanceLexer() //function

        expectError(b, ID) { "ID" }

        parseFuncBody(b, l)

        m.done(LOCAL_FUNC_DEF)
        return m
    }

    private fun parseLocal(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer() // local

        if (!LuaParser.nameList(b, l))
            b.error("ID expected")

        if (b.tokenType == ASSIGN) {
            b.advanceLexer()

            val exprList = b.mark()
            if (LuaExpressionParser.parseExprList(b, l + 1) == null)
                b.error("Expression expected")
            exprList.done(EXPR_LIST)
        }

        m.done(LOCAL_DEF)
        return m
    }

    private fun parseFuncBody(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        expectError(b, LPAREN) { "'('" }

        expectError(b, RPAREN) { "')'" }

        expectError(b, END) { "'end'" }

        m.done(FUNC_BODY)
        return m
    }

    private fun parseGotoStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker {
        val m = b.mark()
        b.advanceLexer()
        expectError(b, ID) { "ID" }
        m.done(GOTO_STAT)
        return m
    }
}