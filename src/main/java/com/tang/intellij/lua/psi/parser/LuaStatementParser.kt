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
import com.tang.intellij.lua.psi.LuaTypes.*

object LuaStatementParser : GeneratedParserUtilBase() {
    fun parseStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        return when (b.tokenType) {
            DO -> parseDoStatement(b, l)
            LOCAL -> {
                if (b.lookAhead(1) == FUNCTION)
                    parseLocalFunction(b, l)
                else
                    parseLocal(b, l)
            }
            GOTO -> parseGotoStatement(b, l)
            else -> null
        }
    }

    private fun parseDoStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        return null
    }

    private fun parseLocalFunction(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        val m = b.mark()
        b.advanceLexer() //local
        b.advanceLexer() //function

        expect(b, ID) { "ID expected" }

        parseFuncBody(b, l)

        m.done(LOCAL_FUNC_DEF)
        return m
    }

    private fun parseLocal(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
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

    private fun parseFuncBody(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        expect(b, LPAREN) { "'(' expected" }

        expect(b, RPAREN) { "')' expected" }
        return null
    }

    private fun parseGotoStatement(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
        val m = b.mark()
        b.advanceLexer()
        expect(b, ID) { "ID expected" }
        m.done(GOTO_STAT)
        return m
    }
}