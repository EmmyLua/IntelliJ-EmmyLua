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
ID=[A-Za-z0-9_\.]+
AT=@
//三个-以上
DOC_DASHES = --+

%state xTAG
%state xTAG_NAME
%state xCOMMENT_STRING

%state xPARAM_NAME
%state xFIELD_NAME

%state xCLASS_NAME
%state xCLASS_EXTEND
%state xCLASS_SUPER

%state xTYPE_SET
%state xTYPE_OR

%%

<YYINITIAL> {
    {EOL}                      { yybegin(YYINITIAL); return com.intellij.psi.TokenType.WHITE_SPACE;}
    {LINE_WS}+                 { return com.intellij.psi.TokenType.WHITE_SPACE; }
    {DOC_DASHES}               { return DASHES; }
    "@"                        { yybegin(xTAG_NAME); return AT; }
    .                          { yybegin(xCOMMENT_STRING); yypushback(yylength()); }
}

<xTAG, xTAG_NAME> {
    {EOL}                      { yybegin(YYINITIAL);return com.intellij.psi.TokenType.WHITE_SPACE;}
    {LINE_WS}+                 { return com.intellij.psi.TokenType.WHITE_SPACE; }
}

<xTAG_NAME> {
    "field"                    { yybegin(xFIELD_NAME); return FIELD; }
    "return"                   { yybegin(xTYPE_SET); return TAG_RETURN; }
    "param"                    { yybegin(xPARAM_NAME); return TAG_PARAM; }
    "class"                    { yybegin(xCLASS_NAME); return CLASS; }
    "type"                     { yybegin(xTYPE_SET); return TYPE;}
    {ID}                       { yybegin(xTAG); return TAG_NAME; }
    [^]                        { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<xCLASS_NAME, xCLASS_EXTEND, xCLASS_SUPER, xFIELD_NAME, xPARAM_NAME, xTYPE_SET, xTYPE_OR> {
    {LINE_WS}+      { return com.intellij.psi.TokenType.WHITE_SPACE; }
}

<xCLASS_NAME> {
    {ID}            { yybegin(xCLASS_EXTEND); return ID; }
}
<xCLASS_EXTEND> {
    ":"             { yybegin(xCLASS_SUPER); return EXTENDS;}
    .               { yybegin(xCOMMENT_STRING); yypushback(yylength()); }
}
<xCLASS_SUPER> {
    {ID}            { yybegin(xCOMMENT_STRING); return ID; }
    .               { yybegin(xCOMMENT_STRING); yypushback(yylength()); }
}

<xFIELD_NAME> {
    "protected"     { return PROTECTED; }
    "public"        { return PUBLIC; }
    {ID}            { yybegin(xTYPE_SET); return ID; }
}

<xPARAM_NAME> {
    "optional"      { return OPTIONAL; }
    {ID}            { yybegin(xTYPE_SET); return ID; }
}

<xTYPE_SET> {
    {ID}            { yybegin(xTYPE_OR); return ID; }
}

<xTYPE_OR> {
    "|"             { yybegin(xTYPE_SET); return OR; }
    .               { yybegin(xCOMMENT_STRING); yypushback(yylength()); }
}

<xTAG> {
    //"@"                        { yybegin(xCOMMENT_STRING); return STRING_BEGIN; }
    //","                        { return COMMA; }
    //"#"                        { return SHARP; }
    //":"                        { return EXTENDS;}
    //"|"                        { return OR; }
    //"optional"                 { return OPTIONAL; }
    //"protected"                { yybegin(xTAG); return PROTECTED; }
    //"public"                   { yybegin(xTAG); return PUBLIC; }
    {ID}                       { return ID; }
    [^]                        { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<xCOMMENT_STRING> {
    {STRING}                   { yybegin(YYINITIAL); return STRING; }
}