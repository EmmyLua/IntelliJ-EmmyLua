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

import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.lang.LuaParserDefinition.Companion.FILE
import com.tang.intellij.lua.psi.LuaTypes.*

class LuaFileParser : PsiParser, LightPsiParser {
    override fun parse(t: IElementType, b: PsiBuilder): ASTNode {
        parseLight(t, b)
        return b.treeBuilt
    }

    override fun parseLight(type: IElementType, b: PsiBuilder) {
        val m = b.mark()
        when (type) {
            FILE -> parseFile(b, 0)
            BLOCK -> parseBlock(b, 0)
            else -> assert(false)
        }

        if (!b.eof()) { // eat more
            val error = b.mark()
            b.advanceLexer()
            error.error("syntax error")

            while (!b.eof()) {
                b.advanceLexer()
            }
        }
        m.done(type)
    }

    companion object {
        private fun parseFile(b: PsiBuilder, l: Int) {
            // SHEBANG
            if (b.tokenType == SHEBANG)
                parseShebang(b)

            parseStatements(b, l)
        }

        private fun parseShebang(b: PsiBuilder, error: Boolean = false): PsiBuilder.Marker {
            val shebang = b.mark()
            b.advanceLexer() // #!
            expectError(b, SHEBANG_CONTENT) { "shebang content expected" }
            shebang.done(SHEBANG_LINE)
            if (error) shebang.precede().error("shebang unexpected")
            return shebang
        }

        private fun parseBlock(b: PsiBuilder, l: Int) {
            parseStatements(b, l)
        }

        private fun parseStatements(b: PsiBuilder, l: Int) {
            while (true) {
                LuaStatementParser.parseStatement(b, l + 1) ?: break
            }
        }
    }
}