package com.tang.intellij.lua.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.tang.intellij.lua.psi.LuaTypes.*;

%%

%{
    public _LuaLexer() {
        this(null);
    }

    private int nBlockEQ = 0;
    public boolean checkAhead(char c, int offset) {
        return this.zzMarkedPos >= this.zzBuffer.length()?false:this.zzBuffer.charAt(this.zzMarkedPos + offset) == c;
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

ID=[A-Za-z_][A-Za-z0-9_]*

//Number
n=[0-9]+
exp=[Ee][+-]?{n}
NUMBER=(0[xX][0-9a-fA-F]+|({n}|{n}[.]{n}){exp}?|[.]{n}|{n}[.])

//Comments
//[[ 与 ]] 中间的内容
SS=([^\]]|\][^\]])*
//--[[]]
BLOCK_COMMENT=--\[=*\[{SS}\]=*\]
SHORT_COMMENT=--.*
DOC_COMMENT=----*.*(\n---*.*)*

//Strings
DOUBLE_QUOTED_STRING=\"([^\\\"\r\n]|\\[^\r\n])*\"?
SINGLE_QUOTED_STRING='([^\\'\r\n]|\\[^\r\n])*'?
//[[]]
LONG_STRING=\[=*\[{SS}\]\]

%state xDOUBLE_QUOTED_STRING
%state xSINGLE_QUOTED_STRING
%state xCOMMENT
%state xBLOCK_COMMENT

%%
<YYINITIAL> {
  {WHITE_SPACE}               { return com.intellij.psi.TokenType.WHITE_SPACE; }
  "--"                        {
        boolean block = false;
        if (checkAhead('[', 0)) {
            int n = 0;
            while (checkAhead('=', n + 1)) n++;
            if (checkAhead('[', n + 1)) {
                block = true;
                nBlockEQ = n;
            }
        }
        if (block) { yypushback(yylength()); yybegin(xBLOCK_COMMENT); }
        else { yypushback(yylength()); yybegin(xCOMMENT); }
   }
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
  {LONG_STRING}               { return STRING; }

  {ID}                        { return ID; }
  {NUMBER}                    { return NUMBER; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<xCOMMENT> {
    {DOC_COMMENT}             {yybegin(YYINITIAL);return DOC_COMMENT;}
    {SHORT_COMMENT}           {yybegin(YYINITIAL);return SHORT_COMMENT;}
}

<xBLOCK_COMMENT> {
    {BLOCK_COMMENT}           {
        boolean valid = true;
        if (nBlockEQ > 0) {
            CharSequence cs = yytext();
            int n = 0;
            while (cs.charAt(cs.length() - n - 2) == '=') n++;
            if (n != nBlockEQ) {
                valid = false;
            }
        }
        if (valid) { yybegin(YYINITIAL);return BLOCK_COMMENT; }
        else { yypushback(yylength()); yybegin(xCOMMENT); }
    }
    [^] { yypushback(yylength()); yybegin(xCOMMENT); }
}

<xDOUBLE_QUOTED_STRING> {
    {DOUBLE_QUOTED_STRING}    { yybegin(YYINITIAL); return STRING; }
}

<xSINGLE_QUOTED_STRING> {
    {SINGLE_QUOTED_STRING}    { yybegin(YYINITIAL); return STRING; }
}