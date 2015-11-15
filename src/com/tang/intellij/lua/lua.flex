package com.tang.intellij.lua.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.psi.LuaTypes;
import com.intellij.psi.TokenType;

%%

//--- file: lua.l ---
/*
* lua.l - flex lexer for Lua 5.1
* Copyright: Same as Lua
*/

%class LuaLexer
%implements FlexLexer, LuaTypes

%unicode

%function advance
%type IElementType

%eof{ return;
%eof}

%{
    ExtendedSyntaxStrCommentHandler longCommentOrStringHandler = new ExtendedSyntaxStrCommentHandler();
%}

%init{
%init}

w           =   [ \t]+
wnl         =   [ \r\n\t]+
nl          =   \r\n|\n|\r
nonl        =   [^\r\n]
nobrknl     =   [^\[\r\n]
name        =   [_a-zA-Z][_a-zA-Z0-9]*
n           =   [0-9]+
exp         =   [Ee][+-]?{n}
number      =   (0[xX][0-9a-fA-F]+|({n}|{n}[.]{n}){exp}?|[.]{n}|{n}[.])
sep         =   =*
luadoc      =   ---[^\r\n]*{nl}([ \t]*--({nobrknl}{nonl}*{nl}|{nonl}{nl}|{nl}))*


%x XLONGSTRING
%x XLONGSTRING_BEGIN
%x XSHORTCOMMENT
%x XLONGCOMMENT
%x XSTRINGQ
%x XSTRINGA


%%

/* Keywords */
"and"          { return AND; }
"break"        { return BREAK; }
"do"           { return DO; }
"else"         { return ELSE; }
"elseif"       { return ELSEIF; }
"end"          { return END; }
"false"        { return FALSE; }
"for"          { return FOR; }
"function"     { return FUNCTION; }
"if"           { return IF; }
"in"           { return IN; }
"local"        { return LOCAL; }
"nil"          { return NIL; }
"not"          { return NOT; }
"or"           { return OR; }
"repeat"       { return REPEAT; }
"return"       { return RETURN; }
"then"         { return THEN; }
"true"         { return TRUE; }
"until"        { return UNTIL; }
"while"        { return WHILE; }
{number}       { return NUMBER; }

{luadoc}       { yypushback(1); /* TODO: Only pushback a newline */  return LUADOC_COMMENT; }

--\[{sep}\[ { longCommentOrStringHandler.setCurrentExtQuoteStart(yytext().toString()); yybegin( XLONGCOMMENT ); return LONGCOMMENT_BEGIN; }
--+        { yypushback(yytext().length()); yybegin( XSHORTCOMMENT ); return advance(); }

"["{sep}"[" { longCommentOrStringHandler.setCurrentExtQuoteStart(yytext().toString()); yybegin( XLONGSTRING_BEGIN ); return LONGSTRING_BEGIN; }

"\""           { yybegin(XSTRINGQ);  return STRING; }
'            { yybegin(XSTRINGA); return STRING; }


"#!"         { yybegin( XSHORTCOMMENT ); return SHEBANG; }
{wnl}        { return TokenType.WHITE_SPACE; }
"..."        { return ELLIPSIS; }
".."         { return CONCAT; }
"=="         { return EQ; }
">="         { return GE; }
"<="         { return LE; }
"~="         { return NE; }
"-"          { return MINUS; }
"+"          { return PLUS;}
"*"          { return MULT;}
"%"          { return MOD;}
"/"          { return DIV; }
"="          { return ASSIGN;}
">"          { return GT;}
"<"          { return LT;}
"("          { return LPAREN;}
")"          { return RPAREN;}
"["          { return LBRACK;}
"]"          { return RBRACK;}
"{"          { return LCURLY;}
"}"          { return RCURLY;}
"#"          { return GETN;}
","          { return COMMA; }
";"          { return SEMI; }
":"          { return COLON; }
"."          { return DOT;}
"^"          { return EXP;}



<XSTRINGQ>
{
  \"\"       {return STRING;}
  \"         { yybegin(YYINITIAL); return STRING; }
  \\[abtnvfr] {return STRING;}
  \\\n       {return STRING;}
  \\\"       {return STRING; }
  \\'        {return STRING;}
  \\"["      {return STRING;}
  \\"]"      {return STRING;}
   \\\\        { return STRING; }
  {nl}    { yybegin(YYINITIAL); return WRONG; }
  .          {return STRING;}
}
<XSTRINGA>
{
  ''          { return STRING; }
  '           { yybegin(YYINITIAL); return STRING; }
  \\[abtnvfr] { return STRING; }
  \\\n        { return STRING; }
  \\\'          { return STRING; }
  \\'          { yybegin(YYINITIAL); return STRING; }
  \\"["       { return STRING; }
  \\"]"       { return STRING; }
  \\\\        { return STRING; }
  {nl}     { yybegin(YYINITIAL);return WRONG;  }
  .          { return STRING; }
}
<XLONGSTRING_BEGIN>
{
    {nl}     { return NL_BEFORE_LONGSTRING; }
    .          { yypushback(yytext().length()); yybegin(XLONGSTRING); return advance(); }
}
<XLONGSTRING>
{
  "]"{sep}"]"     { if (longCommentOrStringHandler.isCurrentExtQuoteStart(yytext())) {
                       yybegin(YYINITIAL); longCommentOrStringHandler.resetCurrentExtQuoteStart(); return LONGSTRING_END;
                       } else { yypushback(yytext().length()-1); }
                        return LONGSTRING;
                  }

  {nl}     { return LONGSTRING; }
  .          { return LONGSTRING; }
}
<XSHORTCOMMENT>
{
  {nl}      {yybegin(YYINITIAL);  yypushback(yytext().length()); return advance(); }

  .          { return SHORTCOMMENT;}
}
<XLONGCOMMENT>
{
  "]"{sep}"]"     { if (longCommentOrStringHandler.isCurrentExtQuoteStart(yytext())) {
                       yybegin(YYINITIAL); longCommentOrStringHandler.resetCurrentExtQuoteStart(); return LONGCOMMENT_END;
                       }  else { yypushback(yytext().length()-1); }
                        return LONGCOMMENT;  }
  {nl}     { return LONGCOMMENT;}
  .          { return LONGCOMMENT;}
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////      identifiers      ////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
{name}       { return NAME; }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Other ////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
.            { return WRONG; }