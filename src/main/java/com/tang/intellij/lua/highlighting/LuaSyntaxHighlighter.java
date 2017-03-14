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

package com.tang.intellij.lua.highlighting;

import com.intellij.ide.highlighter.JavaHighlightingColors;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlTokenType;
import com.tang.intellij.lua.comment.psi.LuaDocTokenType;
import com.tang.intellij.lua.comment.psi.LuaDocTypes;
import com.tang.intellij.lua.psi.LuaStringTypes;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangzx
 * Date : 2015/11/15.
 */
public class LuaSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TokenSet KEYWORD_TOKENS = TokenSet.create(
            LuaTypes.AND,
            LuaTypes.BREAK,
            LuaTypes.DO,
            LuaTypes.ELSE,
            LuaTypes.ELSEIF,
            LuaTypes.END,
            LuaTypes.FOR,
            LuaTypes.FUNCTION,
            LuaTypes.IF,
            LuaTypes.IN,
            LuaTypes.LOCAL,
            LuaTypes.NOT,
            LuaTypes.OR,
            LuaTypes.REPEAT,
            LuaTypes.RETURN,
            LuaTypes.THEN,
            LuaTypes.UNTIL,
            LuaTypes.WHILE
    );
    public static final TokenSet PRIMITIVE_TYPE_SET = TokenSet.create(
            LuaTypes.FALSE,
            LuaTypes.NIL,
            LuaTypes.TRUE
    );
    public static final TokenSet DOC_KEYWORD_TOKENS = TokenSet.create(
            LuaDocTypes.TAG_NAME,
            LuaDocTypes.TAG_PARAM,
            LuaDocTypes.TAG_RETURN,
            LuaDocTypes.CLASS,
            LuaDocTypes.TYPE,
            LuaDocTypes.DEFINE,
            LuaDocTypes.FIELD
    );

    private static final Map<IElementType, TextAttributesKey> ourMap1;
    private static final Map<IElementType, TextAttributesKey> ourMap2;

    static {
        ourMap1 = new HashMap<>();
        ourMap2 = new HashMap<>();

        ourMap1.put(XmlTokenType.XML_DATA_CHARACTERS, JavaHighlightingColors.DOC_COMMENT);
        ourMap1.put(XmlTokenType.XML_REAL_WHITE_SPACE, JavaHighlightingColors.DOC_COMMENT);
        ourMap1.put(XmlTokenType.TAG_WHITE_SPACE, JavaHighlightingColors.DOC_COMMENT);

        ourMap1.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, JavaHighlightingColors.VALID_STRING_ESCAPE);
        ourMap1.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, JavaHighlightingColors.INVALID_STRING_ESCAPE);
        ourMap1.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, JavaHighlightingColors.INVALID_STRING_ESCAPE);

        IElementType[] javaDocMarkup = {
                XmlTokenType.XML_START_TAG_START, XmlTokenType.XML_END_TAG_START, XmlTokenType.XML_TAG_END, XmlTokenType.XML_EMPTY_ELEMENT_END,
                XmlTokenType.TAG_WHITE_SPACE, XmlTokenType.XML_TAG_NAME, XmlTokenType.XML_NAME, XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN,
                XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, XmlTokenType.XML_CHAR_ENTITY_REF,
                XmlTokenType.XML_EQ
        };
        for (IElementType idx : javaDocMarkup) {
            ourMap1.put(idx, JavaHighlightingColors.DOC_COMMENT);
            ourMap2.put(idx, JavaHighlightingColors.DOC_COMMENT_MARKUP);
        }

        //key words
        fillMap(ourMap1, KEYWORD_TOKENS, LuaHighlightingData.KEYWORD);
        fillMap(ourMap1, LuaHighlightingData.KEYWORD, LuaTypes.SEMI, LuaTypes.COMMA);
        fillMap(ourMap1, LuaHighlightingData.OPERATORS, LuaTypes.BINARY_OP, LuaTypes.UNARY_OP);
        fillMap(ourMap1, LuaHighlightingData.BRACKETS, LuaTypes.LBRACK, LuaTypes.LBRACK);
        fillMap(ourMap1, LuaHighlightingData.BRACES, LuaTypes.LCURLY, LuaTypes.RCURLY);
        //comment
        fillMap(ourMap1, LuaHighlightingData.LINE_COMMENT, LuaTypes.SHORT_COMMENT, LuaTypes.BLOCK_COMMENT);
        fillMap(ourMap1, LuaHighlightingData.DOC_COMMENT, LuaTypes.REGION, LuaTypes.ENDREGION);
        fillMap(ourMap1, DOC_KEYWORD_TOKENS, LuaHighlightingData.DOC_COMMENT_TAG);
        //primitive types
        fillMap(ourMap1, LuaHighlightingData.NUMBER, LuaTypes.NUMBER);
        fillMap(ourMap1, LuaHighlightingData.STRING, LuaTypes.STRING);
        fillMap(ourMap1, PRIMITIVE_TYPE_SET, LuaHighlightingData.PRIMITIVE_TYPE);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new LuaFileLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType type) {
        if (type instanceof LuaDocTokenType)
            return pack(LuaHighlightingData.DOC_COMMENT);
        //for string
        else if (type == LuaStringTypes.NEXT_LINE)
            return pack(JavaHighlightingColors.VALID_STRING_ESCAPE);
        else if (type == StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN)
            return pack(JavaHighlightingColors.VALID_STRING_ESCAPE);
        else if (type == StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN)
            return pack(JavaHighlightingColors.INVALID_STRING_ESCAPE);
        else if (type == LuaStringTypes.INVALID_NEXT_LINE)
            return pack(JavaHighlightingColors.INVALID_STRING_ESCAPE);
        else {
            return pack(ourMap1.get(type), ourMap2.get(type));
        }
    }
}
