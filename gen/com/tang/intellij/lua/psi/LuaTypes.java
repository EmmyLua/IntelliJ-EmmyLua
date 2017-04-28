// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.tang.intellij.lua.lang.LuaParserDefinition;
import com.tang.intellij.lua.psi.impl.*;

public interface LuaTypes {

  IElementType ARGS = LuaParserDefinition.createType("ARGS");
  IElementType ASSIGN_STAT = LuaParserDefinition.createType("ASSIGN_STAT");
  IElementType BINARY_EXPR = LuaParserDefinition.createType("BINARY_EXPR");
  IElementType BINARY_OP = LuaParserDefinition.createType("BINARY_OP");
  IElementType BLOCK = LuaParserDefinition.createType("BLOCK");
  IElementType BREAK_STAT = LuaParserDefinition.createType("BREAK_STAT");
  IElementType CALL_EXPR = LuaParserDefinition.createType("CALL_EXPR");
  IElementType CALL_STAT = LuaParserDefinition.createType("CALL_STAT");
  IElementType CLASS_METHOD_DEF = LuaParserDefinition.createType("CLASS_METHOD_DEF");
  IElementType CLASS_METHOD_NAME = LuaParserDefinition.createType("CLASS_METHOD_NAME");
  IElementType CLOSURE_FUNC_DEF = LuaParserDefinition.createType("CLOSURE_FUNC_DEF");
  IElementType DO_STAT = LuaParserDefinition.createType("DO_STAT");
  IElementType EXPR = LuaParserDefinition.createType("EXPR");
  IElementType EXPR_LIST = LuaParserDefinition.createType("EXPR_LIST");
  IElementType FIELD_LIST = LuaParserDefinition.createType("FIELD_LIST");
  IElementType FOR_A_STAT = LuaParserDefinition.createType("FOR_A_STAT");
  IElementType FOR_B_STAT = LuaParserDefinition.createType("FOR_B_STAT");
  IElementType FUNC_BODY = LuaParserDefinition.createType("FUNC_BODY");
  IElementType GLOBAL_FUNC_DEF = LuaParserDefinition.createType("GLOBAL_FUNC_DEF");
  IElementType GOTO_STAT = LuaParserDefinition.createType("GOTO_STAT");
  IElementType IF_STAT = LuaParserDefinition.createType("IF_STAT");
  IElementType INDEX_EXPR = LuaParserDefinition.createType("INDEX_EXPR");
  IElementType LABEL_STAT = LuaParserDefinition.createType("LABEL_STAT");
  IElementType LITERAL_EXPR = LuaParserDefinition.createType("LITERAL_EXPR");
  IElementType LOCAL_DEF = LuaParserDefinition.createType("LOCAL_DEF");
  IElementType LOCAL_FUNC_DEF = LuaParserDefinition.createType("LOCAL_FUNC_DEF");
  IElementType NAME_DEF = LuaParserDefinition.createType("NAME_DEF");
  IElementType NAME_EXPR = LuaParserDefinition.createType("NAME_EXPR");
  IElementType NAME_LIST = LuaParserDefinition.createType("NAME_LIST");
  IElementType PARAM_NAME_DEF = LuaParserDefinition.createType("PARAM_NAME_DEF");
  IElementType PAREN_EXPR = LuaParserDefinition.createType("PAREN_EXPR");
  IElementType REPEAT_STAT = LuaParserDefinition.createType("REPEAT_STAT");
  IElementType RETURN_STAT = LuaParserDefinition.createType("RETURN_STAT");
  IElementType TABLE_CONSTRUCTOR = LuaParserDefinition.createType("TABLE_CONSTRUCTOR");
  IElementType TABLE_FIELD = LuaParserDefinition.createType("TABLE_FIELD");
  IElementType TABLE_FIELD_SEP = LuaParserDefinition.createType("TABLE_FIELD_SEP");
  IElementType UNARY_EXPR = LuaParserDefinition.createType("UNARY_EXPR");
  IElementType UNARY_OP = LuaParserDefinition.createType("UNARY_OP");
  IElementType UNCOMPLETED_STAT = LuaParserDefinition.createType("UNCOMPLETED_STAT");
  IElementType VALUE_EXPR = LuaParserDefinition.createType("VALUE_EXPR");
  IElementType VAR = LuaParserDefinition.createType("VAR");
  IElementType VAR_LIST = LuaParserDefinition.createType("VAR_LIST");
  IElementType WHILE_STAT = LuaParserDefinition.createType("WHILE_STAT");

  IElementType AND = LuaParserDefinition.createToken("and");
  IElementType ASSIGN = LuaParserDefinition.createToken("=");
  IElementType BIT_AND = LuaParserDefinition.createToken("&");
  IElementType BIT_LTLT = LuaParserDefinition.createToken("<<");
  IElementType BIT_OR = LuaParserDefinition.createToken("|");
  IElementType BIT_RTRT = LuaParserDefinition.createToken(">>");
  IElementType BIT_TILDE = LuaParserDefinition.createToken("~");
  IElementType BLOCK_COMMENT = LuaParserDefinition.createToken("BLOCK_COMMENT");
  IElementType BREAK = LuaParserDefinition.createToken("break");
  IElementType COLON = LuaParserDefinition.createToken(":");
  IElementType COMMA = LuaParserDefinition.createToken(",");
  IElementType CONCAT = LuaParserDefinition.createToken("..");
  IElementType DIV = LuaParserDefinition.createToken("/");
  IElementType DO = LuaParserDefinition.createToken("do");
  IElementType DOC_COMMENT = LuaParserDefinition.createToken("DOC_COMMENT");
  IElementType DOT = LuaParserDefinition.createToken(".");
  IElementType DOUBLE_COLON = LuaParserDefinition.createToken("::");
  IElementType DOUBLE_DIV = LuaParserDefinition.createToken("//");
  IElementType ELLIPSIS = LuaParserDefinition.createToken("...");
  IElementType ELSE = LuaParserDefinition.createToken("else");
  IElementType ELSEIF = LuaParserDefinition.createToken("elseif");
  IElementType END = LuaParserDefinition.createToken("end");
  IElementType ENDREGION = LuaParserDefinition.createToken("ENDREGION");
  IElementType EQ = LuaParserDefinition.createToken("==");
  IElementType EXP = LuaParserDefinition.createToken("^");
  IElementType FALSE = LuaParserDefinition.createToken("false");
  IElementType FOR = LuaParserDefinition.createToken("for");
  IElementType FUNCTION = LuaParserDefinition.createToken("function");
  IElementType GE = LuaParserDefinition.createToken(">=");
  IElementType GETN = LuaParserDefinition.createToken("#");
  IElementType GOTO = LuaParserDefinition.createToken("goto");
  IElementType GT = LuaParserDefinition.createToken(">");
  IElementType ID = LuaParserDefinition.createToken("ID");
  IElementType IF = LuaParserDefinition.createToken("if");
  IElementType IN = LuaParserDefinition.createToken("in");
  IElementType LBRACK = LuaParserDefinition.createToken("[");
  IElementType LCURLY = LuaParserDefinition.createToken("{");
  IElementType LE = LuaParserDefinition.createToken("<=");
  IElementType LOCAL = LuaParserDefinition.createToken("local");
  IElementType LPAREN = LuaParserDefinition.createToken("(");
  IElementType LT = LuaParserDefinition.createToken("<");
  IElementType MINUS = LuaParserDefinition.createToken("-");
  IElementType MOD = LuaParserDefinition.createToken("%");
  IElementType MULT = LuaParserDefinition.createToken("*");
  IElementType NE = LuaParserDefinition.createToken("~=");
  IElementType NIL = LuaParserDefinition.createToken("nil");
  IElementType NOT = LuaParserDefinition.createToken("not");
  IElementType NUMBER = LuaParserDefinition.createToken("NUMBER");
  IElementType OR = LuaParserDefinition.createToken("or");
  IElementType PLUS = LuaParserDefinition.createToken("+");
  IElementType RBRACK = LuaParserDefinition.createToken("]");
  IElementType RCURLY = LuaParserDefinition.createToken("}");
  IElementType REGION = LuaParserDefinition.createToken("REGION");
  IElementType REPEAT = LuaParserDefinition.createToken("repeat");
  IElementType RETURN = LuaParserDefinition.createToken("return");
  IElementType RPAREN = LuaParserDefinition.createToken(")");
  IElementType SEMI = LuaParserDefinition.createToken(";");
  IElementType SHEBANG = LuaParserDefinition.createToken("#!");
  IElementType SHORT_COMMENT = LuaParserDefinition.createToken("SHORT_COMMENT");
  IElementType STRING = LuaParserDefinition.createToken("STRING");
  IElementType THEN = LuaParserDefinition.createToken("then");
  IElementType TRUE = LuaParserDefinition.createToken("true");
  IElementType UNTIL = LuaParserDefinition.createToken("until");
  IElementType WHILE = LuaParserDefinition.createToken("while");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ARGS) {
        return new LuaArgsImpl(node);
      }
      else if (type == ASSIGN_STAT) {
        return new LuaAssignStatImpl(node);
      }
      else if (type == BINARY_EXPR) {
        return new LuaBinaryExprImpl(node);
      }
      else if (type == BINARY_OP) {
        return new LuaBinaryOpImpl(node);
      }
      else if (type == BLOCK) {
        return new LuaBlockImpl(node);
      }
      else if (type == BREAK_STAT) {
        return new LuaBreakStatImpl(node);
      }
      else if (type == CALL_EXPR) {
        return new LuaCallExprImpl(node);
      }
      else if (type == CALL_STAT) {
        return new LuaCallStatImpl(node);
      }
      else if (type == CLASS_METHOD_DEF) {
        return new LuaClassMethodDefImpl(node);
      }
      else if (type == CLASS_METHOD_NAME) {
        return new LuaClassMethodNameImpl(node);
      }
      else if (type == CLOSURE_FUNC_DEF) {
        return new LuaClosureFuncDefImpl(node);
      }
      else if (type == DO_STAT) {
        return new LuaDoStatImpl(node);
      }
      else if (type == EXPR_LIST) {
        return new LuaExprListImpl(node);
      }
      else if (type == FIELD_LIST) {
        return new LuaFieldListImpl(node);
      }
      else if (type == FOR_A_STAT) {
        return new LuaForAStatImpl(node);
      }
      else if (type == FOR_B_STAT) {
        return new LuaForBStatImpl(node);
      }
      else if (type == FUNC_BODY) {
        return new LuaFuncBodyImpl(node);
      }
      else if (type == GLOBAL_FUNC_DEF) {
        return new LuaGlobalFuncDefImpl(node);
      }
      else if (type == GOTO_STAT) {
        return new LuaGotoStatImpl(node);
      }
      else if (type == IF_STAT) {
        return new LuaIfStatImpl(node);
      }
      else if (type == INDEX_EXPR) {
        return new LuaIndexExprImpl(node);
      }
      else if (type == LABEL_STAT) {
        return new LuaLabelStatImpl(node);
      }
      else if (type == LITERAL_EXPR) {
        return new LuaLiteralExprImpl(node);
      }
      else if (type == LOCAL_DEF) {
        return new LuaLocalDefImpl(node);
      }
      else if (type == LOCAL_FUNC_DEF) {
        return new LuaLocalFuncDefImpl(node);
      }
      else if (type == NAME_DEF) {
        return new LuaNameDefImpl(node);
      }
      else if (type == NAME_EXPR) {
        return new LuaNameExprImpl(node);
      }
      else if (type == NAME_LIST) {
        return new LuaNameListImpl(node);
      }
      else if (type == PARAM_NAME_DEF) {
        return new LuaParamNameDefImpl(node);
      }
      else if (type == PAREN_EXPR) {
        return new LuaParenExprImpl(node);
      }
      else if (type == REPEAT_STAT) {
        return new LuaRepeatStatImpl(node);
      }
      else if (type == RETURN_STAT) {
        return new LuaReturnStatImpl(node);
      }
      else if (type == TABLE_CONSTRUCTOR) {
        return new LuaTableConstructorImpl(node);
      }
      else if (type == TABLE_FIELD) {
        return new LuaTableFieldImpl(node);
      }
      else if (type == TABLE_FIELD_SEP) {
        return new LuaTableFieldSepImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new LuaUnaryExprImpl(node);
      }
      else if (type == UNARY_OP) {
        return new LuaUnaryOpImpl(node);
      }
      else if (type == UNCOMPLETED_STAT) {
        return new LuaUncompletedStatImpl(node);
      }
      else if (type == VALUE_EXPR) {
        return new LuaValueExprImpl(node);
      }
      else if (type == VAR) {
        return new LuaVarImpl(node);
      }
      else if (type == VAR_LIST) {
        return new LuaVarListImpl(node);
      }
      else if (type == WHILE_STAT) {
        return new LuaWhileStatImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
