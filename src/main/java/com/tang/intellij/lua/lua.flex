package com.tang.intellij.lua.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.lang.LuaLanguageLevel;

import java.io.Reader;

import static com.tang.intellij.lua.psi.LuaTypes.*;

%%

%{
    private LuaLanguageLevel level = LuaLanguageLevel.LUA54;
    public _LuaLexer(LuaLanguageLevel level) {
        this((Reader) null);
        this.level = level;
    }

    private int nBrackets = 0;
    private boolean checkAhead(char c, int offset) {
        return this.zzMarkedPos + offset < this.zzBuffer.length() && this.zzBuffer.charAt(this.zzMarkedPos + offset) == c;
    }

    private boolean checkBlock() {
        nBrackets = 0;
        if (checkAhead('[', 0)) {
            int n = 0;
            while (checkAhead('=', n + 1)) n++;
            if (checkAhead('[', n + 1)) {
                nBrackets = n;
                return true;
            }
        }
        return false;
    }

    private boolean checkDocBlock() {
        return checkAhead('-', nBrackets + 2)
            && checkAhead('-', nBrackets + 3)
            && checkAhead('-', nBrackets + 4);
    }

    private int checkBlockEnd() {
        int pos = zzMarkedPos;
        int end = zzEndRead;
        while(pos < end) {
            char c = zzBuffer.charAt(pos);
            if (c == ']') {
                pos++;
                int size = 0;
                while (pos < zzEndRead && zzBuffer.charAt(pos) == '=') {
                    size++;
                    pos++;
                }
                if (size == nBrackets && zzBuffer.charAt(pos) == ']') {
                    pos++;
                    break;
                }
                continue;
            }
            pos++;
        }
        return pos - zzMarkedPos;
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

VALID_CHAR=[a-zA-Z_\u00ff-\uffff]
ID={VALID_CHAR} ({VALID_CHAR}|[0-9])*

//Number
n=[0-9]+
h=[0-9a-fA-F]+
exp=[Ee]([+-]?{n})?
ppp=[Pp][+-]{n}
// 123ULL/123LL
// 0x123FFULL/0x123FFLL
JIT_EXT_NUMBER=(0[xX]{h}|{n})U?LL
HEX_NUMBER=0[xX]({h}|{h}[.]{h})({exp}|{ppp})?
NUMBER={JIT_EXT_NUMBER}|{HEX_NUMBER}|({n}|{n}[.]{n}){exp}?|[.]{n}|{n}[.]

//Comments
REGION_START =--(region|\{\{\{)([^\r\n]*)*
REGION_END =--(endregion|\}\}\})([^\r\n]*)*
BLOCK_COMMENT=--\[=*\[[\s\S]*(\]=*\])?
DOC_BLOCK_COMMENT=--\[=*\[---+[\s\S]*(\]=*\])?
SHORT_COMMENT=--[^\r\n]*
DOC_COMMENT=----*[^\r\n]*(\r?\n{LINE_WS}*----*[^\r\n]*)*

//Strings
DOUBLE_QUOTED_STRING=\"([^\\\"]|\\\S|\\[\r\n])*\"?  //\"([^\\\"\r\n]|\\[^\r\n])*\"?
SINGLE_QUOTED_STRING='([^\\\']|\\\S|\\[\r\n])*'?    //'([^\\'\r\n]|\\[^\r\n])*'?
//[[]]
LONG_STRING=\[=*\[[\s\S]*\]=*\]

%state xSHEBANG
%state xDOUBLE_QUOTED_STRING
%state xSINGLE_QUOTED_STRING
%state xBLOCK_STRING
%state xCOMMENT
%state xBLOCK_COMMENT

%%
<YYINITIAL> {
  {WHITE_SPACE}               { return TokenType.WHITE_SPACE; }
  {REGION_START}              { return REGION; }
  {REGION_END}                { return ENDREGION; }
  "--"                        {
        boolean block = checkBlock();
        if (block) {
            boolean docBlock = checkDocBlock();
            yypushback(yylength());
            zzMarkedPos += checkBlockEnd();
            return docBlock ? DOC_BLOCK_COMMENT : BLOCK_COMMENT;
        }
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
  "goto"                      { if (level.getVersion() < LuaLanguageLevel.LUA52.getVersion()) return ID; else return GOTO; } //lua5.3
  "#!"                        { yybegin(xSHEBANG); return SHEBANG; }
  "..."                       { return ELLIPSIS; }
  ".."                        { return CONCAT; }
  "=="                        { return EQ; }
  ">="                        { return GE; }
  ">>"                        { return BIT_RTRT; } //lua5.2
  "<="                        { return LE; }
  "<<"                        { return BIT_LTLT; } //lua5.2
  "~="                        { return NE; }
  "~"                         { return BIT_TILDE; } //lua5.2
  "-"                         { return MINUS; }
  "+"                         { return PLUS; }
  "*"                         { return MULT; }
  "%"                         { return MOD; }
  "//"                        { return DOUBLE_DIV; } //lua5.2
  "/"                         { return DIV; }
  "="                         { return ASSIGN; }
  ">"                         { return GT; }
  "<"                         { return LT; }
  "("                         { return LPAREN; }
  ")"                         { return RPAREN; }
  "["                         {
      if (checkAhead('=', 0) || checkAhead('[', 0)) {
          yypushback(yylength());
          checkBlock();
          zzMarkedPos += checkBlockEnd();
          return STRING;
      } else {
          return LBRACK;
      }
  }
  "]"                         { return RBRACK; }
  "{"                         { return LCURLY; }
  "}"                         { return RCURLY; }
  "#"                         { return GETN; }
  ","                         { return COMMA; }
  ";"                         { return SEMI; }
  "::"                        { return DOUBLE_COLON; } //lua5.2
  ":"                         { return COLON; }
  "."                         { return DOT; }
  "^"                         { return EXP; }
  "~"                         { return BIT_TILDE; } //lua5.2
  "&"                         { return BIT_AND; } //lua5.2
  "|"                         { return BIT_OR; } //lua5.2

  "\""                        { yybegin(xDOUBLE_QUOTED_STRING); yypushback(yylength()); }
  "'"                         { yybegin(xSINGLE_QUOTED_STRING); yypushback(yylength()); }

  {ID}                        { return ID; }
  {NUMBER}                    { return NUMBER; }

  [^]                         { return TokenType.BAD_CHARACTER; }
}

<xSHEBANG> {
    [^\r\n]*                  { yybegin(YYINITIAL); return SHEBANG_CONTENT; }
}

<xCOMMENT> {
    {DOC_COMMENT}             {yybegin(YYINITIAL);return DOC_COMMENT;}
    {SHORT_COMMENT}           {yybegin(YYINITIAL);return SHORT_COMMENT;}
}

<xDOUBLE_QUOTED_STRING> {
    {DOUBLE_QUOTED_STRING}    { yybegin(YYINITIAL); return STRING; }
}

<xSINGLE_QUOTED_STRING> {
    {SINGLE_QUOTED_STRING}    { yybegin(YYINITIAL); return STRING; }
}
