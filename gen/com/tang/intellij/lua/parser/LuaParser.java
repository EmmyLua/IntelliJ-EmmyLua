// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import static com.tang.intellij.lua.psi.LuaParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static com.intellij.lang.WhitespacesBinders.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class LuaParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return luaFile(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(EXPR_LIST, VAR_LIST),
    create_token_set_(NAME_DEF, PARAM_NAME_DEF),
    create_token_set_(ARGS, LIST_ARGS, SINGLE_ARG),
    create_token_set_(BINARY_EXPR, CALL_EXPR, CLOSURE_EXPR, EXPR,
      INDEX_EXPR, LITERAL_EXPR, NAME_EXPR, PAREN_EXPR,
      TABLE_EXPR, UNARY_EXPR),
    create_token_set_(ASSIGN_STAT, BREAK_STAT, DO_STAT, EMPTY_STAT,
      EXPR_STAT, FOR_A_STAT, FOR_B_STAT, GOTO_STAT,
      IF_STAT, LABEL_STAT, REPEAT_STAT, RETURN_STAT,
      WHILE_STAT),
  };

  /* ********************************************************** */
  // (expr ',')* (expr |& ')')
  static boolean arg_expr_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = arg_expr_list_0(b, l + 1);
    p = r; // pin = 1
    r = r && arg_expr_list_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (expr ',')*
  private static boolean arg_expr_list_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arg_expr_list_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arg_expr_list_0", c)) break;
    }
    return true;
  }

  // expr ','
  private static boolean arg_expr_list_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  // expr |& ')'
  private static boolean arg_expr_list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1);
    if (!r) r = arg_expr_list_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // & ')'
  private static boolean arg_expr_list_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // listArgs | singleArg
  public static boolean args(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "args")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, ARGS, "<args>");
    r = listArgs(b, l + 1);
    if (!r) r = singleArg(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // varList '=' exprList
  public static boolean assignStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignStat")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ASSIGN_STAT, "<assign stat>");
    r = varList(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 2
    r = r && exprList(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    register_hook_(b, RIGHT_BINDER, MY_RIGHT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '<' ID '>'
  public static boolean attribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ATTRIBUTE, null);
    r = consumeTokens(b, 1, LT, ID, GT);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // binaryOp expr
  public static boolean binaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binaryExpr")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _LEFT_, BINARY_EXPR, "<binary expr>");
    r = binaryOp(b, l + 1);
    p = r; // pin = 1
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '+' | '-' | '*' | '/' | '^' | '%' | '..' |
  //     '<' | '<=' | '>' | '>=' | '==' | '~=' |
  //     'and' | 'or'
  //     // lua5.3
  //     | '|' | '&' | '>>' | '<<' | '~' | '//'
  public static boolean binaryOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binaryOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINARY_OP, "<binary op>");
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, MULT);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, EXP);
    if (!r) r = consumeToken(b, MOD);
    if (!r) r = consumeToken(b, CONCAT);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, LE);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, GE);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, NE);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, BIT_OR);
    if (!r) r = consumeToken(b, BIT_AND);
    if (!r) r = consumeToken(b, BIT_RTRT);
    if (!r) r = consumeToken(b, BIT_LTLT);
    if (!r) r = consumeToken(b, BIT_TILDE);
    if (!r) r = consumeToken(b, DOUBLE_DIV);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // stat_semi*
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    while (true) {
      int c = current_position_(b);
      if (!stat_semi(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "block", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // break
  public static boolean breakStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStat")) return false;
    if (!nextTokenIs(b, BREAK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BREAK);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, m, BREAK_STAT, r);
    return r;
  }

  /* ********************************************************** */
  // args
  public static boolean callExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, CALL_EXPR, "<call expr>");
    r = args(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ID ('.' | ':') ID
  static boolean checkFuncPrefix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "checkFuncPrefix")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ID);
    r = r && checkFuncPrefix_1(b, l + 1);
    r = r && consumeToken(b, ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // '.' | ':'
  private static boolean checkFuncPrefix_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "checkFuncPrefix_1")) return false;
    boolean r;
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, COLON);
    return r;
  }

  /* ********************************************************** */
  // 'function' classMethodName funcBody
  public static boolean classMethodDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMethodDef")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_METHOD_DEF, null);
    r = consumeToken(b, FUNCTION);
    r = r && classMethodName(b, l + 1);
    p = r; // pin = 2
    r = r && funcBody(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // nameExpr (funcPrefixRef)* (('.' ID) | (':' ID))
  public static boolean classMethodName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMethodName")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_METHOD_NAME, null);
    r = nameExpr(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, classMethodName_1(b, l + 1));
    r = p && classMethodName_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (funcPrefixRef)*
  private static boolean classMethodName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMethodName_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!classMethodName_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classMethodName_1", c)) break;
    }
    return true;
  }

  // (funcPrefixRef)
  private static boolean classMethodName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMethodName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = funcPrefixRef(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.' ID) | (':' ID)
  private static boolean classMethodName_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMethodName_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classMethodName_2_0(b, l + 1);
    if (!r) r = classMethodName_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '.' ID
  private static boolean classMethodName_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMethodName_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, ID);
    exit_section_(b, m, null, r);
    return r;
  }

  // ':' ID
  private static boolean classMethodName_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMethodName_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COLON, ID);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'function' funcBody
  public static boolean closureExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "closureExpr")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLOSURE_EXPR, null);
    r = consumeToken(b, FUNCTION);
    p = r; // pin = 1
    r = r && funcBody(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // classMethodDef | funcDef | localFuncDef | localDef
  static boolean defStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defStat")) return false;
    if (!nextTokenIs(b, "", FUNCTION, LOCAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classMethodDef(b, l + 1);
    if (!r) r = funcDef(b, l + 1);
    if (!r) r = localFuncDef(b, l + 1);
    if (!r) r = localDef(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'do' <<lazyBlock>> 'end'
  public static boolean doStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doStat")) return false;
    if (!nextTokenIs(b, DO)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DO_STAT, null);
    r = consumeToken(b, DO);
    p = r; // pin = 1
    r = r && report_error_(b, lazyBlock(b, l + 1));
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ';'
  public static boolean emptyStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "emptyStat")) return false;
    if (!nextTokenIs(b, SEMI)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMI);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, m, EMPTY_STAT, r);
    return r;
  }

  /* ********************************************************** */
  // <<parseExpr>>
  public static boolean expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, EXPR, "<expr>");
    r = parseExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // expr (',' expr)*
  public static boolean exprList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_LIST, "<expr list>");
    r = expr(b, l + 1);
    r = r && exprList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' expr)*
  private static boolean exprList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!exprList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exprList_1", c)) break;
    }
    return true;
  }

  // ',' expr
  private static boolean exprList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expr
  public static boolean exprStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprStat")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_STAT, "<expr stat>");
    r = expr(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (tableField (tableFieldSep tableField)* (tableFieldSep)?)?
  static boolean fieldList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldList")) return false;
    fieldList_0(b, l + 1);
    return true;
  }

  // tableField (tableFieldSep tableField)* (tableFieldSep)?
  private static boolean fieldList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tableField(b, l + 1);
    r = r && fieldList_0_1(b, l + 1);
    r = r && fieldList_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (tableFieldSep tableField)*
  private static boolean fieldList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldList_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!fieldList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fieldList_0_1", c)) break;
    }
    return true;
  }

  // tableFieldSep tableField
  private static boolean fieldList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tableFieldSep(b, l + 1);
    r = r && tableField(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (tableFieldSep)?
  private static boolean fieldList_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldList_0_2")) return false;
    fieldList_0_2_0(b, l + 1);
    return true;
  }

  // (tableFieldSep)
  private static boolean fieldList_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldList_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tableFieldSep(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'for' paramNameDef '=' expr ',' expr (',' expr)? 'do' <<lazyBlock>> 'end'
  public static boolean forAStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forAStat")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_A_STAT, null);
    r = consumeToken(b, FOR);
    r = r && paramNameDef(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 3
    r = r && report_error_(b, expr(b, l + 1));
    r = p && report_error_(b, consumeToken(b, COMMA)) && r;
    r = p && report_error_(b, expr(b, l + 1)) && r;
    r = p && report_error_(b, forAStat_6(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, DO)) && r;
    r = p && report_error_(b, lazyBlock(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (',' expr)?
  private static boolean forAStat_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forAStat_6")) return false;
    forAStat_6_0(b, l + 1);
    return true;
  }

  // ',' expr
  private static boolean forAStat_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forAStat_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'for' parList 'in' exprList 'do' <<lazyBlock>> 'end'
  public static boolean forBStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forBStat")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_B_STAT, null);
    r = consumeToken(b, FOR);
    p = r; // pin = 1
    r = r && report_error_(b, parList(b, l + 1));
    r = p && report_error_(b, consumeToken(b, IN)) && r;
    r = p && report_error_(b, exprList(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, DO)) && r;
    r = p && report_error_(b, lazyBlock(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '(' (parList)? ')' <<lazyBlock>>? 'end'
  public static boolean funcBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcBody")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNC_BODY, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, funcBody_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && report_error_(b, funcBody_3(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (parList)?
  private static boolean funcBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcBody_1")) return false;
    funcBody_1_0(b, l + 1);
    return true;
  }

  // (parList)
  private static boolean funcBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcBody_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<lazyBlock>>?
  private static boolean funcBody_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcBody_3")) return false;
    lazyBlock(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'function' ID funcBody
  public static boolean funcDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcDef")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNC_DEF, null);
    r = consumeTokens(b, 1, FUNCTION, ID);
    p = r; // pin = 1
    r = r && funcBody(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '.' <<repeat checkFuncPrefix 1>> ID
  public static boolean funcPrefixRef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcPrefixRef")) return false;
    if (!nextTokenIs(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, INDEX_EXPR, null);
    r = consumeToken(b, DOT);
    r = r && repeat(b, l + 1, LuaParser::checkFuncPrefix, 1);
    r = r && consumeToken(b, ID);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'goto' ID
  public static boolean gotoStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gotoStat")) return false;
    if (!nextTokenIs(b, GOTO)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GOTO_STAT, null);
    r = consumeTokens(b, 1, GOTO, ID);
    p = r; // pin = 1
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'if' expr 'then' <<lazyBlock>> ('elseif' expr 'then' <<lazyBlock>>)* ('else' <<lazyBlock>>)? 'end'
  public static boolean ifStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_STAT, null);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1));
    r = p && report_error_(b, consumeToken(b, THEN)) && r;
    r = p && report_error_(b, lazyBlock(b, l + 1)) && r;
    r = p && report_error_(b, ifStat_4(b, l + 1)) && r;
    r = p && report_error_(b, ifStat_5(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('elseif' expr 'then' <<lazyBlock>>)*
  private static boolean ifStat_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifStat_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifStat_4", c)) break;
    }
    return true;
  }

  // 'elseif' expr 'then' <<lazyBlock>>
  private static boolean ifStat_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSEIF);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, THEN);
    r = r && lazyBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('else' <<lazyBlock>>)?
  private static boolean ifStat_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat_5")) return false;
    ifStat_5_0(b, l + 1);
    return true;
  }

  // 'else' <<lazyBlock>>
  private static boolean ifStat_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && lazyBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '[' expr ']' | '.' ID | ':' ID
  public static boolean indexExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "indexExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, INDEX_EXPR, "<index expr>");
    r = indexExpr_0(b, l + 1);
    if (!r) r = parseTokens(b, 1, DOT, ID);
    if (!r) r = parseTokens(b, 1, COLON, ID);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '[' expr ']'
  private static boolean indexExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "indexExpr_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LBRACK);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1));
    r = p && consumeToken(b, RBRACK) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '::' ID '::'
  public static boolean labelStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "labelStat")) return false;
    if (!nextTokenIs(b, DOUBLE_COLON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LABEL_STAT, null);
    r = consumeTokens(b, 1, DOUBLE_COLON, ID, DOUBLE_COLON);
    p = r; // pin = 1
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // returnStat | breakStat
  static boolean lastStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lastStat")) return false;
    if (!nextTokenIs(b, "", BREAK, RETURN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnStat(b, l + 1);
    if (!r) r = breakStat(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '(' (arg_expr_list)? ')'
  public static boolean listArgs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listArgs")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIST_ARGS, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, listArgs_1(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (arg_expr_list)?
  private static boolean listArgs_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listArgs_1")) return false;
    listArgs_1_0(b, l + 1);
    return true;
  }

  // (arg_expr_list)
  private static boolean listArgs_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listArgs_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arg_expr_list(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // nil | false | true | NUMBER | STRING | "..."
  public static boolean literalExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPR, "<literal expr>");
    r = consumeToken(b, NIL);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, ELLIPSIS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'local' nameList ('=' exprList)?
  public static boolean localDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "localDef")) return false;
    if (!nextTokenIs(b, LOCAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LOCAL_DEF, null);
    r = consumeToken(b, LOCAL);
    p = r; // pin = 1
    r = r && report_error_(b, nameList(b, l + 1));
    r = p && localDef_2(b, l + 1) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    register_hook_(b, RIGHT_BINDER, MY_RIGHT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('=' exprList)?
  private static boolean localDef_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "localDef_2")) return false;
    localDef_2_0(b, l + 1);
    return true;
  }

  // '=' exprList
  private static boolean localDef_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "localDef_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && exprList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'local' 'function' ID funcBody
  public static boolean localFuncDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "localFuncDef")) return false;
    if (!nextTokenIs(b, LOCAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LOCAL_FUNC_DEF, null);
    r = consumeTokens(b, 2, LOCAL, FUNCTION, ID);
    p = r; // pin = 2
    r = r && funcBody(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // shebang_line? stat_semi*
  static boolean luaFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "luaFile")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = luaFile_0(b, l + 1);
    r = r && luaFile_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // shebang_line?
  private static boolean luaFile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "luaFile_0")) return false;
    shebang_line(b, l + 1);
    return true;
  }

  // stat_semi*
  private static boolean luaFile_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "luaFile_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!stat_semi(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "luaFile_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ID
  public static boolean nameDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameDef")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ID);
    exit_section_(b, m, NAME_DEF, r);
    return r;
  }

  /* ********************************************************** */
  // ID
  public static boolean nameExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameExpr")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ID);
    exit_section_(b, m, NAME_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // nameDef attribute? (',' nameDef attribute?)*
  public static boolean nameList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameList")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nameDef(b, l + 1);
    r = r && nameList_1(b, l + 1);
    r = r && nameList_2(b, l + 1);
    exit_section_(b, m, NAME_LIST, r);
    return r;
  }

  // attribute?
  private static boolean nameList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameList_1")) return false;
    attribute(b, l + 1);
    return true;
  }

  // (',' nameDef attribute?)*
  private static boolean nameList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!nameList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "nameList_2", c)) break;
    }
    return true;
  }

  // ',' nameDef attribute?
  private static boolean nameList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && nameDef(b, l + 1);
    r = r && nameList_2_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // attribute?
  private static boolean nameList_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameList_2_0_2")) return false;
    attribute(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // paramNameDef (',' paramNameDef)* (',' '...')? | '...'
  static boolean parList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = parList_0(b, l + 1);
    if (!r) r = consumeToken(b, ELLIPSIS);
    exit_section_(b, l, m, r, false, LuaParser::parList_recover);
    return r;
  }

  // paramNameDef (',' paramNameDef)* (',' '...')?
  private static boolean parList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = paramNameDef(b, l + 1);
    r = r && parList_0_1(b, l + 1);
    r = r && parList_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' paramNameDef)*
  private static boolean parList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parList_0_1", c)) break;
    }
    return true;
  }

  // ',' paramNameDef
  private static boolean parList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && paramNameDef(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' '...')?
  private static boolean parList_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList_0_2")) return false;
    parList_0_2_0(b, l + 1);
    return true;
  }

  // ',' '...'
  private static boolean parList_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, ELLIPSIS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(')'|'in')
  static boolean parList_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !parList_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')'|'in'
  private static boolean parList_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList_recover_0")) return false;
    boolean r;
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, IN);
    return r;
  }

  /* ********************************************************** */
  // ID
  public static boolean paramNameDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paramNameDef")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ID);
    exit_section_(b, m, PARAM_NAME_DEF, r);
    return r;
  }

  /* ********************************************************** */
  // '(' expr ')'
  public static boolean parenExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenExpr")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PAREN_EXPR, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // parenExpr | nameExpr | tableExpr | literalExpr
  static boolean prefixExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefixExpr")) return false;
    boolean r;
    r = parenExpr(b, l + 1);
    if (!r) r = nameExpr(b, l + 1);
    if (!r) r = tableExpr(b, l + 1);
    if (!r) r = literalExpr(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // prefixExpr (suffixExpr*)
  static boolean primaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prefixExpr(b, l + 1);
    r = r && primaryExpr_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // suffixExpr*
  private static boolean primaryExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!suffixExpr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "primaryExpr_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'repeat' <<lazyBlock>> 'until' expr
  public static boolean repeatStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "repeatStat")) return false;
    if (!nextTokenIs(b, REPEAT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REPEAT_STAT, null);
    r = consumeToken(b, REPEAT);
    p = r; // pin = 1
    r = r && report_error_(b, lazyBlock(b, l + 1));
    r = p && report_error_(b, consumeToken(b, UNTIL)) && r;
    r = p && expr(b, l + 1) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // return exprList?
  public static boolean returnStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStat")) return false;
    if (!nextTokenIs(b, RETURN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RETURN);
    r = r && returnStat_1(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, m, RETURN_STAT, r);
    return r;
  }

  // exprList?
  private static boolean returnStat_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStat_1")) return false;
    exprList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // SHEBANG SHEBANG_CONTENT
  public static boolean shebang_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shebang_line")) return false;
    if (!nextTokenIs(b, SHEBANG)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, SHEBANG, SHEBANG_CONTENT);
    exit_section_(b, m, SHEBANG_LINE, r);
    return r;
  }

  /* ********************************************************** */
  // tableExpr | stringExpr
  public static boolean singleArg(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleArg")) return false;
    if (!nextTokenIs(b, "<single arg>", LCURLY, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SINGLE_ARG, "<single arg>");
    r = tableExpr(b, l + 1);
    if (!r) r = stringExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // emptyStat |
  //     doStat |
  //     whileStat |
  //     repeatStat |
  //     ifStat |
  //     forAStat |
  //     forBStat |
  //     defStat |
  //     lastStat |
  //     labelStat |
  //     gotoStat |
  //     assignStat |
  //     exprStat
  static boolean stat_impl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stat_impl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = emptyStat(b, l + 1);
    if (!r) r = doStat(b, l + 1);
    if (!r) r = whileStat(b, l + 1);
    if (!r) r = repeatStat(b, l + 1);
    if (!r) r = ifStat(b, l + 1);
    if (!r) r = forAStat(b, l + 1);
    if (!r) r = forBStat(b, l + 1);
    if (!r) r = defStat(b, l + 1);
    if (!r) r = lastStat(b, l + 1);
    if (!r) r = labelStat(b, l + 1);
    if (!r) r = gotoStat(b, l + 1);
    if (!r) r = assignStat(b, l + 1);
    if (!r) r = exprStat(b, l + 1);
    exit_section_(b, l, m, r, false, LuaParser::stat_recover);
    return r;
  }

  /* ********************************************************** */
  // !(ID
  //     | ',' | ';'
  //     | 'local' | 'do' | 'while' | 'repeat' | 'function' | 'if' | 'for' | 'return' | break
  //     | nil | true | false | STRING | NUMBER | '::' | 'goto'
  //     | unaryOp)
  static boolean stat_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stat_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !stat_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ID
  //     | ',' | ';'
  //     | 'local' | 'do' | 'while' | 'repeat' | 'function' | 'if' | 'for' | 'return' | break
  //     | nil | true | false | STRING | NUMBER | '::' | 'goto'
  //     | unaryOp
  private static boolean stat_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stat_recover_0")) return false;
    boolean r;
    r = consumeToken(b, ID);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, LOCAL);
    if (!r) r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, REPEAT);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, NIL);
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, DOUBLE_COLON);
    if (!r) r = consumeToken(b, GOTO);
    if (!r) r = unaryOp(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // <<parseStatement>>
  static boolean stat_semi(PsiBuilder b, int l) {
    return parseStatement(b, l + 1);
  }

  /* ********************************************************** */
  // STRING
  public static boolean stringExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringExpr")) return false;
    if (!nextTokenIs(b, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING);
    exit_section_(b, m, LITERAL_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // indexExpr | callExpr
  static boolean suffixExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "suffixExpr")) return false;
    boolean r;
    r = indexExpr(b, l + 1);
    if (!r) r = callExpr(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '{' fieldList '}'
  public static boolean tableExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tableExpr")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_EXPR, null);
    r = consumeToken(b, LCURLY);
    p = r; // pin = 1
    r = r && report_error_(b, fieldList(b, l + 1));
    r = p && consumeToken(b, RCURLY) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // tableField1 | tableField2 | expr
  public static boolean tableField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tableField")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TABLE_FIELD, "<table field>");
    r = tableField1(b, l + 1);
    if (!r) r = tableField2(b, l + 1);
    if (!r) r = expr(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    register_hook_(b, RIGHT_BINDER, MY_RIGHT_COMMENT_BINDER);
    exit_section_(b, l, m, r, false, LuaParser::tableField_recover);
    return r;
  }

  /* ********************************************************** */
  // '[' expr ']' '=' expr
  static boolean tableField1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tableField1")) return false;
    if (!nextTokenIs(b, LBRACK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LBRACK);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, RBRACK, ASSIGN)) && r;
    r = p && expr(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ID '=' expr
  static boolean tableField2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tableField2")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 2, ID, ASSIGN);
    p = r; // pin = 2
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ',' | ';'
  public static boolean tableFieldSep(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tableFieldSep")) return false;
    if (!nextTokenIs(b, "<table field sep>", COMMA, SEMI)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TABLE_FIELD_SEP, "<table field sep>");
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMI);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(tableFieldSep | '}' | '[')
  static boolean tableField_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tableField_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !tableField_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // tableFieldSep | '}' | '['
  private static boolean tableField_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tableField_recover_0")) return false;
    boolean r;
    r = tableFieldSep(b, l + 1);
    if (!r) r = consumeToken(b, RCURLY);
    if (!r) r = consumeToken(b, LBRACK);
    return r;
  }

  /* ********************************************************** */
  // unaryOp (unaryExpr | primaryExpr)
  public static boolean unaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNARY_EXPR, "<unary expr>");
    r = unaryOp(b, l + 1);
    p = r; // pin = 1
    r = r && unaryExpr_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // unaryExpr | primaryExpr
  private static boolean unaryExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr_1")) return false;
    boolean r;
    r = unaryExpr(b, l + 1);
    if (!r) r = primaryExpr(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '-' | 'not' | '#'
  //     // lua5.3
  //     | '~'
  public static boolean unaryOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNARY_OP, "<unary op>");
    r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, GETN);
    if (!r) r = consumeToken(b, BIT_TILDE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // primaryExpr
  static boolean varExpr(PsiBuilder b, int l) {
    return primaryExpr(b, l + 1);
  }

  /* ********************************************************** */
  // varExpr (',' varExpr)*
  public static boolean varList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VAR_LIST, "<var list>");
    r = varExpr(b, l + 1);
    r = r && varList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' varExpr)*
  private static boolean varList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!varList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varList_1", c)) break;
    }
    return true;
  }

  // ',' varExpr
  private static boolean varList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && varExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'while' expr 'do' <<lazyBlock>> 'end'
  public static boolean whileStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileStat")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STAT, null);
    r = consumeToken(b, WHILE);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1));
    r = p && report_error_(b, consumeToken(b, DO)) && r;
    r = p && report_error_(b, lazyBlock(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

}
