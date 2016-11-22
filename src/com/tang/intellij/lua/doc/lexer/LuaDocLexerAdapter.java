package com.tang.intellij.lua.doc.lexer;

import com.intellij.lexer.FlexAdapter;

public class LuaDocLexerAdapter extends FlexAdapter {
    public LuaDocLexerAdapter() {
        super(new _LuaDocLexer());
    }
}
