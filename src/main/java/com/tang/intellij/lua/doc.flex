package com.tang.intellij.lua.comment.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.comment.psi.LuaDocTypes;

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
%state xTAG_NAME
%state xCOMMENT_STRING

%%

<YYINITIAL> {
    {WHITE_SPACE}              { return com.intellij.psi.TokenType.WHITE_SPACE; }
    {DOC_DASHES}               { return DASHES; }
    "@"                        { yybegin(xTAG_NAME); return AT; }
    .                          { yybegin(xCOMMENT_STRING); yypushback(yylength()); }
}

<xTAG, xTAG_NAME> {
    {EOL}                      { yybegin(YYINITIAL);return com.intellij.psi.TokenType.WHITE_SPACE;}
    {LINE_WS}                  { return com.intellij.psi.TokenType.WHITE_SPACE; }
}

<xTAG_NAME> {
    "field"                    { yybegin(xTAG); return FIELD; }
    "define"                   { yybegin(xTAG); return DEFINE; }
    "return"                   { yybegin(xTAG); return TAG_RETURN; }
    "param"                    { yybegin(xTAG); return TAG_PARAM; }
    "class"                    { yybegin(xTAG); return CLASS; }
    "interface"                { yybegin(xTAG); return INTERFACE; }
    "type"                     { yybegin(xTAG); return TYPE;}
    {ID}                       { yybegin(xTAG); return TAG_NAME; }
    [^]                        { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<xTAG> {
    "@"                        { yybegin(xCOMMENT_STRING); return STRING_BEGIN; }
    ","                        { return COMMA; }
    "#"                        { return SHARP; }
    ":"                        { return EXTENDS;}
    "protected"                { yybegin(xTAG); return PROTECTED; }
    "public"                   { yybegin(xTAG); return PUBLIC; }
    {ID}                       { return ID; }
    [^]                        { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<xCOMMENT_STRING> {
 {STRING}                   { yybegin(YYINITIAL); return STRING; }
}