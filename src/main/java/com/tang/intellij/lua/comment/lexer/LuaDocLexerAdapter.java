package com.tang.intellij.lua.comment.lexer;

import com.intellij.lexer.FlexAdapter;

public class LuaDocLexerAdapter extends FlexAdapter {
    public LuaDocLexerAdapter() {
        super(new _LuaDocLexer());
    }
}
