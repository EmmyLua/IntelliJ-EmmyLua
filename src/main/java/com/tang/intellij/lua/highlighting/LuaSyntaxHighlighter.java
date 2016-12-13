package com.tang.intellij.lua.highlighting;

import com.intellij.execution.process.ConsoleHighlighter;
import com.intellij.ide.highlighter.custom.CustomHighlighterColors;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.tang.intellij.lua.comment.psi.LuaDocTokenType;
import com.tang.intellij.lua.comment.psi.LuaDocTypes;
import com.tang.intellij.lua.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

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

    private static final TextAttributesKey KEYWORD = createTextAttributesKey("LUA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    private static final TextAttributesKey NUMBER = createTextAttributesKey("LUA_NUMBER", DefaultLanguageHighlighterColors.NUMBER);

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new LuaFileLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType type) {
        if (KEYWORD_TOKENS.contains(type))
            return pack(KEYWORD);
        else if (type == LuaTypes.NUMBER)
            return pack(NUMBER);
        else if (type == LuaTypes.STRING)
            return pack(DefaultLanguageHighlighterColors.STRING);
        else if (PRIMITIVE_TYPE_SET.contains(type))
            return pack(ConsoleHighlighter.CYAN_BRIGHT);
        else if (type == LuaTypes.SELF)
            return pack(LuaHighlightingData.SELF);

        // for comment
        else if (type == LuaTypes.SHORT_COMMENT)
            return pack(DefaultLanguageHighlighterColors.LINE_COMMENT);
        else if (DOC_KEYWORD_TOKENS.contains(type))
            return pack(LuaHighlightingData.LUADOC_TAG);
        else if (type instanceof LuaDocTokenType)
            return pack(DefaultLanguageHighlighterColors.DOC_COMMENT);

        return new TextAttributesKey[0];
    }
}
