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

package com.tang.intellij.lua.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.tree.IElementType
import com.tang.intellij.lua.comment.psi.LuaDocTokenType
import com.tang.intellij.lua.comment.psi.LuaDocTypes
import com.tang.intellij.lua.lang.LuaParserDefinition.Companion.DOC_KEYWORD_TOKENS
import com.tang.intellij.lua.lang.LuaParserDefinition.Companion.DOC_TAG_TOKENS
import com.tang.intellij.lua.lang.LuaParserDefinition.Companion.KEYWORD_TOKENS
import com.tang.intellij.lua.lang.LuaParserDefinition.Companion.PRIMITIVE_TYPE_SET
import com.tang.intellij.lua.psi.LuaRegionTypes
import com.tang.intellij.lua.psi.LuaStringTypes
import com.tang.intellij.lua.psi.LuaTypes
import java.util.*

/**
 * Created by tangzx
 * Date : 2015/11/15.
 */
class LuaSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer {
        return LuaFileLexer()
    }

    override fun getTokenHighlights(type: IElementType): Array<TextAttributesKey> {
        return when {
            ourMap1.containsKey(type) -> SyntaxHighlighterBase.pack(ourMap1[type], ourMap2[type])
            type is LuaDocTokenType -> SyntaxHighlighterBase.pack(LuaHighlightingData.DOC_COMMENT)
            type === LuaStringTypes.NEXT_LINE -> SyntaxHighlighterBase.pack(DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
            type === StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN -> SyntaxHighlighterBase.pack(DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
            type === StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN -> SyntaxHighlighterBase.pack(DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
            type === LuaStringTypes.INVALID_NEXT_LINE -> SyntaxHighlighterBase.pack(DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)//for string
            else -> SyntaxHighlighterBase.pack(null)
        }
    }

    companion object {



        private val ourMap1: Map<IElementType, TextAttributesKey>
        private val ourMap2: Map<IElementType, TextAttributesKey>

        init {
            ourMap1 = HashMap()
            ourMap2 = HashMap()

            //key words
            fillMap(ourMap1, KEYWORD_TOKENS, LuaHighlightingData.KEYWORD)
            fillMap(ourMap1, LuaHighlightingData.SEMICOLON, LuaTypes.SEMI)
            fillMap(ourMap1, LuaHighlightingData.COMMA, LuaTypes.COMMA)
            fillMap(ourMap1, LuaHighlightingData.OPERATORS, LuaTypes.BINARY_OP, LuaTypes.UNARY_OP)
            fillMap(ourMap1, LuaHighlightingData.BRACKETS, LuaTypes.LBRACK, LuaTypes.RBRACK)
            fillMap(ourMap1, LuaHighlightingData.BRACES, LuaTypes.LCURLY, LuaTypes.RCURLY)
            fillMap(ourMap1, LuaHighlightingData.PARENTHESES, LuaTypes.LPAREN, LuaTypes.RPAREN)
            fillMap(ourMap1, LuaHighlightingData.DOT, LuaTypes.DOT)
            //comment
            fillMap(ourMap1, LuaHighlightingData.LINE_COMMENT, LuaTypes.SHEBANG)
            fillMap(ourMap1, LuaHighlightingData.DOC_COMMENT, LuaTypes.SHEBANG_CONTENT)

            fillMap(ourMap1, LuaHighlightingData.LINE_COMMENT, LuaTypes.SHORT_COMMENT, LuaTypes.BLOCK_COMMENT)
            fillMap(ourMap1, LuaHighlightingData.DOC_COMMENT, LuaTypes.REGION, LuaTypes.ENDREGION)
            fillMap(ourMap1, DOC_TAG_TOKENS, LuaHighlightingData.DOC_COMMENT_TAG)
            fillMap(ourMap1, LuaHighlightingData.DOC_COMMENT_TAG, LuaDocTypes.TAG_NAME)
            fillMap(ourMap1, DOC_KEYWORD_TOKENS, LuaHighlightingData.DOC_KEYWORD)
            fillMap(ourMap1, LuaHighlightingData.BRACKETS, LuaDocTypes.ARR)
            fillMap(ourMap1, LuaHighlightingData.PARENTHESES, LuaDocTypes.LPAREN, LuaDocTypes.RPAREN)

            //primitive types
            fillMap(ourMap1, LuaHighlightingData.NUMBER, LuaTypes.NUMBER)
            fillMap(ourMap1, LuaHighlightingData.STRING, LuaTypes.STRING)
            fillMap(ourMap1, PRIMITIVE_TYPE_SET, LuaHighlightingData.PRIMITIVE_TYPE)

            //region
            fillMap(ourMap1, LuaHighlightingData.REGION_HEADER, LuaRegionTypes.REGION_START)
            fillMap(ourMap1, LuaHighlightingData.REGION_DESC, LuaRegionTypes.REGION_DESC)
            fillMap(ourMap1, LuaHighlightingData.REGION_HEADER, LuaRegionTypes.REGION_END)
        }
    }
}
