package com.tang.intellij.lua.lexer;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.psi.LuaTokenType;
import static com.tang.intellij.lua.psi.LuaTypes.*;

%%

%{
  private StringBuilder string = new StringBuilder();
  public _LuaLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _LuaLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+

SHORT_COMMENT=--.*
DOC_COMMENT=----*.*(\n---*.*)*
ID=[A-Za-z_][A-Za-z0-9_]*
NUMBER=-?(\d*(\.\d+)?|(0x[a-fA-F0-9]+))
DOUBLE_QUOTED_STRING=\"([^\\\"\r\n]|\\[^\r\n])*\"?
SINGLE_QUOTED_STRING='([^\\'\r\n]|\\[^\r\n])*'?

%state xDOUBLE_QUOTED_STRING
%state xSINGLE_QUOTED_STRING

%%
<YYINITIAL> {
  {WHITE_SPACE}               { return com.intellij.psi.TokenType.WHITE_SPACE; }
  {DOC_COMMENT}               { return DOC_COMMENT; }
  {SHORT_COMMENT}             { return SHORT_COMMENT; }
  "and"                       { return AND; }
  "break"                     { return BREAK; }
  "do"                        { return DO; }
  "else"                      { return ELSE; }
  "elseif"                    { return ELSEIF; }
  "end"                       { return END; }
  "false"                     { return FALSE; }
  "for"                       { return FOR; }
  "function"                  { return FUNCTION; }
  "if"                        { return IF; }
  "in"                        { return IN; }
  "local"                     { return LOCAL; }
  "nil"                       { return NIL; }
  "not"                       { return NOT; }
  "or"                        { return OR; }
  "repeat"                    { return REPEAT; }
  "return"                    { return RETURN; }
  "then"                      { return THEN; }
  "true"                      { return TRUE; }
  "until"                     { return UNTIL; }
  "while"                     { return WHILE; }
  "self"                      { return SELF; }
  "#!"                        { return SHEBANG; }
  "..."                       { return ELLIPSIS; }
  ".."                        { return CONCAT; }
  "=="                        { return EQ; }
  ">="                        { return GE; }
  "<="                        { return LE; }
  "~="                        { return NE; }
  "-"                         { return MINUS; }
  "+"                         { return PLUS; }
  "*"                         { return MULT; }
  "%"                         { return MOD; }
  "/"                         { return DIV; }
  "="                         { return ASSIGN; }
  ">"                         { return GT; }
  "<"                         { return LT; }
  "("                         { return LPAREN; }
  ")"                         { return RPAREN; }
  "["                         { return LBRACK; }
  "]"                         { return RBRACK; }
  "{"                         { return LCURLY; }
  "}"                         { return RCURLY; }
  "#"                         { return GETN; }
  ","                         { return COMMA; }
  ";"                         { return SEMI; }
  ":"                         { return COLON; }
  "."                         { return DOT; }
  "^"                         { return EXP; }

  "\""                        { yybegin(xDOUBLE_QUOTED_STRING); yypushback(yylength()); }
  "'"                         { yybegin(xSINGLE_QUOTED_STRING); yypushback(yylength()); }

  {ID}                        { return ID; }
  {NUMBER}                    { return NUMBER; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<xDOUBLE_QUOTED_STRING> {
    {DOUBLE_QUOTED_STRING} { yybegin(YYINITIAL); return STRING; }
}

<xSINGLE_QUOTED_STRING> {
    {SINGLE_QUOTED_STRING} { yybegin(YYINITIAL); return STRING; }
}