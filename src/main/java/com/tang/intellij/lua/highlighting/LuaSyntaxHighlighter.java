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

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.tang.intellij.lua.comment.psi.LuaDocTokenType;
import com.tang.intellij.lua.comment.psi.LuaDocTypes;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new LuaFileLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType type) {
        if (KEYWORD_TOKENS.contains(type))
            return pack(LuaHighlightingData.KEYWORD);
        else if (type == LuaTypes.NUMBER)
            return pack(LuaHighlightingData.NUMBER);
        else if (type == LuaTypes.STRING)
            return pack(LuaHighlightingData.STRING);
        else if (type == LuaTypes.BINARY_OP || type == LuaTypes.UNARY_OP)
            return pack(LuaHighlightingData.OPERATORS);
        else if (type == LuaTypes.LBRACK || type == LuaTypes.RBRACK)
            return pack(LuaHighlightingData.BRACKETS);
        else if (type == LuaTypes.LCURLY || type == LuaTypes.RCURLY)
            return pack(LuaHighlightingData.BRACES);
        else if (PRIMITIVE_TYPE_SET.contains(type))
            return pack(LuaHighlightingData.PRIMITIVE_TYPE);
        else if (type == LuaTypes.SELF)
            return pack(LuaHighlightingData.SELF);

        // for comment
        else if (type == LuaTypes.SHORT_COMMENT || type == LuaTypes.BLOCK_COMMENT)
            return pack(LuaHighlightingData.LINE_COMMENT);
        else if (DOC_KEYWORD_TOKENS.contains(type))
            return pack(LuaHighlightingData.DOC_COMMENT_TAG);
        else if (type instanceof LuaDocTokenType || type == LuaTypes.REGION || type == LuaTypes.ENDREGION)
            return pack(LuaHighlightingData.DOC_COMMENT);

        return new TextAttributesKey[0];
    }
}
