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

  public boolean checkAhead(char c) {
     if (zzMarkedPos >= zzBuffer.length()) return false;
     return zzBuffer.charAt(zzMarkedPos) == c;
  }

  public void goTo(int offset) {
    zzCurrentPos = zzMarkedPos = zzStartRead = offset;
    //zzPushbackPos = 0;
    zzAtEOF = offset < zzEndRead;
  }


%}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////// LuaDoc lexems ////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

%state COMMENT_DATA_START
%state COMMENT_DATA
%state TAG_DOC_SPACE
%state PRE_TAG_DATA_SPACE
%state DOC_TAG_VALUE
%state TAG_AT
%state ATT_L
%state ATT_NAME
%state ATT_EQ
%state ATT_VALUE
%state ATT_SPACE
%state ATT_R

WHITE_DOC_SPACE_CHAR=[\ \t\f\n\r]
WHITE_DOC_SPACE_NO_NL=[\ \t\f]
NON_WHITE_DOC_SPACE_CHAR=[^\ \t\f\n\r]
DIGIT=[0-9]
ALPHA=[:jletter:]
TAGNAME={ALPHA}({ALPHA}|{DIGIT})*
AT=@
%%

<YYINITIAL> --- { yybegin(COMMENT_DATA_START); return LDOC_COMMENT_START; }
<COMMENT_DATA_START> {WHITE_DOC_SPACE_NO_NL}+ { return LDOC_WHITESPACE; }
<COMMENT_DATA_START> (--+) { return LDOC_DASHES; }
<COMMENT_DATA>  {WHITE_DOC_SPACE_NO_NL}+ { return LDOC_COMMENT_DATA; }
<COMMENT_DATA, COMMENT_DATA_START, TAG_DOC_SPACE>  \r?\n { yybegin(COMMENT_DATA_START); return LDOC_WHITESPACE; }

<TAG_DOC_SPACE> {WHITE_DOC_SPACE_NO_NL}+ { yybegin(COMMENT_DATA); return LDOC_WHITESPACE; }
<DOC_TAG_VALUE> {NON_WHITE_DOC_SPACE_CHAR}+ { yybegin(TAG_DOC_SPACE); return LDOC_TAG_VALUE; }

<COMMENT_DATA_START> {AT} { yybegin(TAG_AT); return LDOC_AT; }

<PRE_TAG_DATA_SPACE>  {WHITE_DOC_SPACE_CHAR}+ {yybegin(DOC_TAG_VALUE); return LDOC_WHITESPACE;}

//---> ATT
<COMMENT_DATA> "[" { yybegin(ATT_L); return LDOC_ATT_L; }
<ATT_L> {TAGNAME} { yybegin(ATT_NAME); return LDOC_ATT_NAME; }
<ATT_NAME> ":" { yybegin(ATT_EQ); return LDOC_ATT_EQ; }
<ATT_EQ> {TAGNAME} { yybegin(ATT_VALUE); return LDOC_ATT_VALUE; }
<ATT_VALUE> "]" { yybegin(ATT_R); return LDOC_ATT_R; }
<ATT_VALUE> "," { yybegin(ATT_L); return LDOC_ATT_L; }
//<--- ATT

<TAG_AT> {TAGNAME} { yybegin(PRE_TAG_DATA_SPACE); return LDOC_TAG_NAME;  }

<COMMENT_DATA_START, COMMENT_DATA, DOC_TAG_VALUE, ATT_R> . { yybegin(COMMENT_DATA); return LDOC_COMMENT_DATA; }

[^] { return LDOC_COMMENT_BAD_CHARACTER; }