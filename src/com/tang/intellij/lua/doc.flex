package com.tang.intellij.lua.doc.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.doc.psi.LuaDocTypes;

%%

%class _LuaDocLexer
%implements FlexLexer, LuaDocTypes


%unicode
%public

%function advance
%type IElementType

%eof{ return;
%eof}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// User code //////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

%{ // User code

  public _LuaDocLexer() {
    this((java.io.Reader)null);
  }
%}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////// LuaDoc lexems ////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+
STRING=[^\r\n\t\f]*
ID=[A-Za-z0-9_]+
AT=@
//三个-以上
DOC_DASHES = ----*

%state xTAG
%state xCOMMENT_STRING

%%

<YYINITIAL> {
 {WHITE_SPACE}              { return com.intellij.psi.TokenType.WHITE_SPACE; }
 {DOC_DASHES}               { return DASHES; }
 "@"                        { yybegin(xTAG); return AT; }
 .                          { yybegin(xCOMMENT_STRING); yypushback(yylength()); }
}

<xTAG> {
 {EOL}                      { yybegin(YYINITIAL);return com.intellij.psi.TokenType.WHITE_SPACE;}
 {LINE_WS}                  { return com.intellij.psi.TokenType.WHITE_SPACE; }
 "@"                        { yybegin(xCOMMENT_STRING); return STRING_BEGIN; }
 ","                        { return COMMA; }
 "#"                        { return SHARP; }
 "field"                    { return FIELD; }
 "define"                   { return DEFINE; }
 "return"                   { return TAG_RETURN; }
 "param"                    { return TAG_PARAM; }
 "private"                  { return PRIVATE; }
 "public"                   { return PUBLIC; }
 "class"                    { return CLASS; }
 "interface"                { return INTERFACE; }
 "extends"                  { return EXTENDS;}
 "type"                     { return TYPE;}
 {ID}                       { return ID; }
 [^]                        { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<xCOMMENT_STRING> {
 {STRING}                   { yybegin(YYINITIAL); return STRING; }
}