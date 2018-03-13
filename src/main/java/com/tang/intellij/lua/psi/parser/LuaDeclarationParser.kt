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
import com.tang.intellij.lua.psi.LuaTypes.*

object LuaDeclarationParser {
    fun parseTableFieldList(b: PsiBuilder, l: Int): Boolean {
        val result = parseTableField(b, l)
        while (result != null) {
            val sep = parseTableSep(b, l)
            val nextField = parseTableField(b, l)
            if (sep == null || nextField == null)
                break
        }
        return true
    }

    private fun parseTableSep(b: PsiBuilder, l: Int): PsiBuilder.Marker? {
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
                val expr = LuaExpressionParser.parseExpr(b, l + 1)
                if (expr == null)
                    b.error("Expression expected")

                if (b.tokenType == RBRACK) {
                    b.advanceLexer()
                } else b.error("']' expected")

                if (b.tokenType == ASSIGN) {
                    b.advanceLexer()
                } else b.error("'=' expected")

                if (LuaExpressionParser.parseExpr(b, l + 1) == null)
                    b.error("Expression expected")

                m.done(TABLE_FIELD)
                return m
            }
            ID -> { // ID '=' expr
                val m = b.mark()
                b.advanceLexer()
                if (b.tokenType == ASSIGN) {
                    b.advanceLexer()
                    if (LuaExpressionParser.parseExpr(b, l + 1) == null) {
                        b.error("Expression expected")
                    }
                    m.done(TABLE_FIELD)
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
            return m
        }
        return null
    }
}