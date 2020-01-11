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
    private int _typeLevel = 0;
    private boolean _typeReq = false;
    public _LuaDocLexer() {
        this((java.io.Reader) null);
    }

    private void beginType() {
        yybegin(xTYPE_REF);
        _typeLevel = 0;
        _typeReq = true;
    }

    private int nBrackets = -1;
    private boolean checkAhead(char c, int offset) {
        return this.zzMarkedPos + offset < this.zzBuffer.length() && this.zzBuffer.charAt(this.zzMarkedPos + offset) == c;
    }
%}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////// LuaDoc lexems ////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+
STRING=([^\r\n\t\f\]]|\]=*[^\]])*
ID=[:jletter:] ([:jletterdigit:]|\.)*
AT=@
//三个-以上
DOC_DASHES = --+
BLOCK_BEGIN = --\[=*\[---+
BLOCK_END = \]=*\]
//Strings
DOUBLE_QUOTED_STRING=\"([^\\\"]|\\\S|\\[\r\n])*\"?  //\"([^\\\"\r\n]|\\[^\r\n])*\"?
SINGLE_QUOTED_STRING='([^\\\']|\\\S|\\[\r\n])*'?    //'([^\\'\r\n]|\\[^\r\n])*'?
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
// Boolean
BOOLEAN=true|false

%state xBODY
%state xTAG
%state xTAG_WITH_ID
%state xTAG_NAME
%state xCOMMENT_STRING
%state xPARAM
%state xTYPE_REF
%state xCLASS
%state xCLASS_EXTEND
%state xFIELD
%state xFIELD_ID
%state xGENERIC
%state xALIAS
%state xSUPPRESS
%state xDOUBLE_QUOTED_STRING
%state xSINGLE_QUOTED_STRING
%state xBACKTICK_QUOTED_STRING

%%

<YYINITIAL> {
    {BLOCK_BEGIN}              { nBrackets = yylength() - 7; yybegin(xBODY); return BLOCK_BEGIN; }
    .                          { yybegin(xBODY); yypushback(yylength()); }
}

<xBODY> {
    {EOL}                      { yybegin(xBODY); return EOL;}
    {LINE_WS}+                 { return com.intellij.psi.TokenType.WHITE_SPACE; }
    {BLOCK_END}                {
        if (yylength() - 2 == nBrackets) {
      	    nBrackets = -1;
            return BLOCK_END;
        }
    }
    {DOC_DASHES}               { if (nBrackets == -1) return DASHES; }
    "@"                        { yybegin(xTAG_NAME); return AT; }
    .                          { yybegin(xCOMMENT_STRING); yypushback(yylength()); }
}

<xTAG, xTAG_WITH_ID, xTAG_NAME, xPARAM, xTYPE_REF, xCLASS, xCLASS_EXTEND, xFIELD, xFIELD_ID, xCOMMENT_STRING, xGENERIC, xALIAS, xSUPPRESS> {
    {LINE_WS}+                 { return com.intellij.psi.TokenType.WHITE_SPACE; }
    {BLOCK_END}                {
        if (yylength() - 2 == nBrackets) {
      	    nBrackets = -1;
            return BLOCK_END;
        }
    }
}

<xTAG_NAME> {
    "field"                    { yybegin(xFIELD); return TAG_NAME_FIELD; }
    "param"                    { yybegin(xPARAM); return TAG_NAME_PARAM; }
    "vararg"                   { yybegin(xPARAM); return TAG_NAME_VARARG; }
    "class"                    { yybegin(xCLASS); return TAG_NAME_CLASS; }
    "module"                   { yybegin(xCLASS); return TAG_NAME_MODULE; }
    "return"                   { beginType(); return TAG_NAME_RETURN; }
    "type"                     { beginType(); return TAG_NAME_TYPE;}
    "overload"                 { beginType(); return TAG_NAME_OVERLOAD; }
    "private"                  { return TAG_NAME_PRIVATE; }
    "protected"                { return TAG_NAME_PROTECTED; }
    "public"                   { return TAG_NAME_PUBLIC; }
    "language"                 { yybegin(xTAG_WITH_ID); return TAG_NAME_LANGUAGE;}
    "generic"                  { yybegin(xGENERIC); return TAG_NAME_GENERIC; }
    "see"                      { yybegin(xTAG); return TAG_NAME_SEE; }
    "alias"                    { yybegin(xALIAS); return TAG_NAME_ALIAS; }
    "suppress"                 { yybegin(xSUPPRESS); return TAG_NAME_SUPPRESS; }
    {ID}                       { yybegin(xCOMMENT_STRING); return TAG_NAME; }
    [^]                        { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<xSUPPRESS> {
    {ID}                       { return ID; }
    ","                        { return COMMA; }
    [^]                        { yybegin(xBODY); yypushback(yylength()); }
}

<xALIAS> {
    {ID}                       { beginType(); return ID; }
    [^]                        { yybegin(xBODY); yypushback(yylength()); }
}

<xGENERIC> {
    {ID}                       { return ID; }
    ":"                        { return EXTENDS;}
    ","                        { return COMMA; }
    [^]                        { yybegin(xBODY); yypushback(yylength()); }
}

<xCLASS> {
    {ID}                       { yybegin(xCLASS_EXTEND); return ID; }
}
<xCLASS_EXTEND> {
    ":"                        { beginType(); return EXTENDS;}
    [^]                        { yybegin(xCOMMENT_STRING); yypushback(yylength()); }
}

<xPARAM> {
    {ID}                       { beginType(); return ID; }
    "..."                      { beginType(); return ID; } //varargs
}

<xFIELD> {
    "private"                  { yybegin(xFIELD_ID); return PRIVATE; }
    "protected"                { yybegin(xFIELD_ID); return PROTECTED; }
    "public"                   { yybegin(xFIELD_ID); return PUBLIC; }
    {ID}                       { beginType(); return ID; }
}
<xFIELD_ID> {
    {ID}                       { beginType(); return ID; }
}

<xTYPE_REF> {
    "@"                        { yybegin(xCOMMENT_STRING); return STRING_BEGIN; }
    ","                        { _typeReq = true; return COMMA; }
    "|"                        { _typeReq = true; return OR; }
    ":"                        { _typeReq = true; return EXTENDS;}
    "<"                        { _typeLevel++; return LT; }
    ">"                        { _typeLevel--; _typeReq = false; return GT; }
    "("                        { _typeLevel++; return LPAREN; }
    ")"                        { _typeLevel--; _typeReq = false; return RPAREN; }
    "{"                        { _typeLevel++; return LCURLY; }
    "}"                        { _typeLevel--; _typeReq = false; return RCURLY; }
    "\""                       { yybegin(xDOUBLE_QUOTED_STRING); yypushback(yylength()); }
    "'"                        { yybegin(xSINGLE_QUOTED_STRING); yypushback(yylength()); }
    "`"                        { yybegin(xBACKTICK_QUOTED_STRING); yypushback(yylength()); }
    {BOOLEAN}                  { return BOOLEAN_LITERAL; }
    {NUMBER}                   { return NUMBER_LITERAL; }
    "[]"                       { _typeReq = false; return ARR; }
    "fun"                      { return FUN; }
    "vararg"                   { _typeReq = true; return VARARG; }
    "..."|{ID}                 { if (_typeReq || _typeLevel > 0) { _typeReq = false; return ID; } else { yybegin(xCOMMENT_STRING); yypushback(yylength()); } }
}

<xDOUBLE_QUOTED_STRING> {
    {DOUBLE_QUOTED_STRING}    { yybegin(xTYPE_REF); return STRING_LITERAL; }
}

<xSINGLE_QUOTED_STRING> {
    {SINGLE_QUOTED_STRING}    { yybegin(xTYPE_REF); return STRING_LITERAL; }
}

<xBACKTICK_QUOTED_STRING> {
    {SINGLE_QUOTED_STRING}    { yybegin(xTYPE_REF); return SNIPPET; }
}

<xTAG> {
    "@"                        { yybegin(xCOMMENT_STRING); return STRING_BEGIN; }
    "#"                        { return SHARP; }
    {ID}                       { return ID; }
    [^]                        { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
<xTAG_WITH_ID> {
    {ID}                       { yybegin(xCOMMENT_STRING); return ID; }
}

<xCOMMENT_STRING> {
    {STRING}                   { yybegin(xBODY); return STRING; }
    {BLOCK_END}                { return STRING; }
}

<xTAG, xTAG_WITH_ID, xTAG_NAME, xPARAM, xTYPE_REF, xCLASS, xCLASS_EXTEND, xFIELD, xFIELD_ID, xCOMMENT_STRING, xGENERIC, xALIAS, xSUPPRESS> {
    {EOL}                      { yybegin(xBODY); return EOL; }
}

[^]                            { return com.intellij.psi.TokenType.BAD_CHARACTER; }
