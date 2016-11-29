package com.tang.intellij.lua.highlighting;

import com.intellij.lexer.LayeredLexer;
import com.tang.intellij.lua.doc.lexer.LuaDocLexerAdapter;
import com.tang.intellij.lua.lexer.LuaLexerAdapter;
import com.tang.intellij.lua.psi.LuaElementType;

/**
 * for highlight
 * Created by tangzx on 2016/11/29.
 */
public class LuaFileLexer extends LayeredLexer {
    public LuaFileLexer() {
        super(new LuaLexerAdapter());
        registerLayer(new LuaDocLexerAdapter(), LuaElementType.DOC_COMMENT);
    }
}
