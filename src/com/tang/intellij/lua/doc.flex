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

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+
ID=[A-Za-z0-9_]+
AT=@
%%

<YYINITIAL> {
 {WHITE_SPACE}              { return com.intellij.psi.TokenType.WHITE_SPACE; }
 ---                        { return LDOC_COMMENT_START; }
 --                         { return LDOC_DASHES; }
 "@"                        { return AT; }
 "="                        { return EQ; }
 {ID}                       { return ID; }
 [^]                        { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}