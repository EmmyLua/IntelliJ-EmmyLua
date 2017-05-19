package com.tang.intellij.lua.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.StringEscapesTokenTypes.*;
import static com.tang.intellij.lua.psi.LuaStringTypes.*;
import static com.tang.intellij.lua.psi.LuaTypes.*;
%%

%class _LuaStringLexer
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

  public _LuaStringLexer() {
    this(null);
  }
%}

%state STRING_CONTENT
%state BLOCK_STRING_CONTENT

%%

<YYINITIAL> {
    "\""    { yybegin(STRING_CONTENT); return STRING; }
    "'"     { yybegin(STRING_CONTENT); return STRING; }
    \[=*\[  { /*yybegin(BLOCK_STRING_CONTENT); */return STRING; }
    [^]     { return STRING; }
}

<STRING_CONTENT> {
    [^\\\n]+    { return STRING; }
    \\\n        { return NEXT_LINE; }
    \n          { return INVALID_NEXT_LINE; }
    \\\d+       { return VALID_STRING_ESCAPE_TOKEN; }
    \\\S        { return VALID_STRING_ESCAPE_TOKEN; }
    [^]         { return STRING; }
}

<BLOCK_STRING_CONTENT> {
    [^\\\n]+    { return STRING; }
    \\\d+       { return VALID_STRING_ESCAPE_TOKEN; }
    \\\S        { return VALID_STRING_ESCAPE_TOKEN; }
    [^]         { return STRING; }
}