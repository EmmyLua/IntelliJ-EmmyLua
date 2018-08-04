package com.tang.intellij.lua.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.tang.intellij.lua.psi.LuaRegionTypes.*;
%%

%class _LuaRegionLexer
%implements FlexLexer


%unicode
%public

%function advance
%type IElementType

%eof{
%eof}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// User code //////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

%{ // User code

  public _LuaRegionLexer() {
    this(null);
  }
%}

LINE_WS=[\ \t\f]

%state xREGION_DESC

%%

<YYINITIAL> {
    "--region"        { yybegin(xREGION_DESC); return REGION_START; }
    "--{{{"        { yybegin(xREGION_DESC); return REGION_START; }
    "--endregion"     { yybegin(xREGION_DESC); return REGION_END; }
    "--}}}"     { yybegin(xREGION_DESC); return REGION_END; }
    [^]               { return TokenType.BAD_CHARACTER; }
}

<xREGION_DESC> {
    {LINE_WS}+                { return TokenType.WHITE_SPACE; }
    [^\r\n]*                  { yybegin(YYINITIAL); return REGION_DESC; }
}