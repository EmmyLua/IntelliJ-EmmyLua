package com.tang.intellij.lua.lexer;

import com.intellij.lexer.FlexAdapter;

/**
 * Created by TangZhiXu on 2015/11/15.
 * Email:272669294@qq.com
 */
public class LuaLexerAdapter extends FlexAdapter {
    public LuaLexerAdapter() {
        super(new LuaLexer(null));
    }
}
