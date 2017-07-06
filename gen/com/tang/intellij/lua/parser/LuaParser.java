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
    if (t == ARGS) {
      r = args(b, 0);
    }
    else if (t == ASSIGN_STAT) {
      r = assignStat(b, 0);
    }
    else if (t == BINARY_OP) {
      r = binaryOp(b, 0);
    }
    else if (t == BLOCK) {
      r = block(b, 0);
    }
    else if (t == BREAK_STAT) {
      r = breakStat(b, 0);
    }
    else if (t == CALL_EXPR) {
      r = callExpr(b, 0);
    }
    else if (t == CALL_STAT) {
      r = callStat(b, 0);
    }
    else if (t == CLASS_METHOD_DEF) {
      r = classMethodDef(b, 0);
    }
    else if (t == CLASS_METHOD_NAME) {
      r = classMethodName(b, 0);
    }
    else if (t == CLOSURE_EXPR) {
      r = closureExpr(b, 0);
    }
    else if (t == DO_STAT) {
      r = doStat(b, 0);
    }
    else if (t == EXPR) {
      r = expr(b, 0, -1);
    }
    else if (t == EXPR_LIST) {
      r = exprList(b, 0);
    }
    else if (t == FOR_A_STAT) {
      r = forAStat(b, 0);
    }
    else if (t == FOR_B_STAT) {
      r = forBStat(b, 0);
    }
    else if (t == FUNC_BODY) {
      r = funcBody(b, 0);
    }
    else if (t == GLOBAL_FUNC_DEF) {
      r = globalFuncDef(b, 0);
    }
    else if (t == GOTO_STAT) {
      r = gotoStat(b, 0);
    }
    else if (t == IF_STAT) {
      r = ifStat(b, 0);
    }
    else if (t == INDEX_EXPR) {
      r = indexExpr(b, 0);
    }
    else if (t == LABEL_STAT) {
      r = labelStat(b, 0);
    }
    else if (t == LITERAL_EXPR) {
      r = literalExpr(b, 0);
    }
    else if (t == LOCAL_DEF) {
      r = localDef(b, 0);
    }
    else if (t == LOCAL_FUNC_DEF) {
      r = localFuncDef(b, 0);
    }
    else if (t == NAME_DEF) {
      r = nameDef(b, 0);
    }
    else if (t == NAME_EXPR) {
      r = nameExpr(b, 0);
    }
    else if (t == NAME_LIST) {
      r = nameList(b, 0);
    }
    else if (t == PARAM_NAME_DEF) {
      r = paramNameDef(b, 0);
    }
    else if (t == PAREN_EXPR) {
      r = parenExpr(b, 0);
    }
    else if (t == REPEAT_STAT) {
      r = repeatStat(b, 0);
    }
    else if (t == RETURN_STAT) {
      r = returnStat(b, 0);
    }
    else if (t == TABLE_EXPR) {
      r = tableExpr(b, 0);
    }
    else if (t == TABLE_FIELD) {
      r = tableField(b, 0);
    }
    else if (t == TABLE_FIELD_SEP) {
      r = tableFieldSep(b, 0);
    }
    else if (t == UNARY_OP) {
      r = unaryOp(b, 0);
    }
    else if (t == UNCOMPLETED_STAT) {
      r = uncompletedStat(b, 0);
    }
    else if (t == VAR_LIST) {
      r = varList(b, 0);
    }
    else if (t == WHILE_STAT) {
      r = whileStat(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return luaFile(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(EXPR_LIST, VAR_LIST),
    create_token_set_(NAME_DEF, PARAM_NAME_DEF),
    create_token_set_(BINARY_EXPR, CALL_EXPR, CLOSURE_EXPR, EXPR,
      INDEX_EXPR, LITERAL_EXPR, NAME_EXPR, PAREN_EXPR,
      TABLE_EXPR, UNARY_EXPR, VALUE_EXPR),
    create_token_set_(ASSIGN_STAT, BREAK_STAT, CALL_STAT, DO_STAT,
      FOR_A_STAT, FOR_B_STAT, GOTO_STAT, IF_STAT,
      LABEL_STAT, REPEAT_STAT, RETURN_STAT, UNCOMPLETED_STAT,
      WHILE_STAT),
  };

  /* ********************************************************** */
  // (expr ',')* (expr |& ')')
  public static boolean arg_expr_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_LIST, "<arg expr list>");
    r = arg_expr_list_0(b, l + 1);
    r = r && arg_expr_list_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (expr ',')*
  private static boolean arg_expr_list_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!arg_expr_list_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arg_expr_list_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // expr ','
  private static boolean arg_expr_list_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1, -1);
    r = r && consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  // expr |& ')'
  private static boolean arg_expr_list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arg_expr_list_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1, -1);
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
  // '(' (arg_expr_list)? ')' | tableExpr | STRING
  public static boolean args(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "args")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARGS, "<args>");
    r = args_0(b, l + 1);
    if (!r) r = tableExpr(b, l + 1);
    if (!r) r = consumeToken(b, STRING);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' (arg_expr_list)? ')'
  private static boolean args_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "args_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, args_0_1(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (arg_expr_list)?
  private static boolean args_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "args_0_1")) return false;
    args_0_1_0(b, l + 1);
    return true;
  }

  // (arg_expr_list)
  private static boolean args_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "args_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arg_expr_list(b, l + 1);
    exit_section_(b, m, null, r);
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
  // stat_semi* (lastStat ';'?)?
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    r = block_0(b, l + 1);
    r = r && block_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // stat_semi*
  private static boolean block_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!stat_semi(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "block_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (lastStat ';'?)?
  private static boolean block_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_1")) return false;
    block_1_0(b, l + 1);
    return true;
  }

  // lastStat ';'?
  private static boolean block_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lastStat(b, l + 1);
    r = r && block_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean block_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_1_0_1")) return false;
    consumeToken(b, SEMI);
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
  // prefixExpr suffixExpr+
  static boolean callOrIndexExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callOrIndexExpr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prefixExpr(b, l + 1);
    r = r && callOrIndexExpr_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // suffixExpr+
  private static boolean callOrIndexExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callOrIndexExpr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = suffixExpr(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!suffixExpr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "callOrIndexExpr_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // funcCallExpr
  public static boolean callStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callStat")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CALL_STAT, "<call stat>");
    r = funcCallExpr(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, COLON);
    exit_section_(b, m, null, r);
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
    boolean r;
    Marker m = enter_section_(b);
    r = nameExpr(b, l + 1);
    r = r && classMethodName_1(b, l + 1);
    r = r && classMethodName_2(b, l + 1);
    exit_section_(b, m, CLASS_METHOD_NAME, r);
    return r;
  }

  // (funcPrefixRef)*
  private static boolean classMethodName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMethodName_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!classMethodName_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classMethodName_1", c)) break;
      c = current_position_(b);
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
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FUNCTION);
    r = r && funcBody(b, l + 1);
    exit_section_(b, m, CLOSURE_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // classMethodDef | globalFuncDef | localFuncDef | localDef
  static boolean defStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defStat")) return false;
    if (!nextTokenIs(b, "", FUNCTION, LOCAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classMethodDef(b, l + 1);
    if (!r) r = globalFuncDef(b, l + 1);
    if (!r) r = localFuncDef(b, l + 1);
    if (!r) r = localDef(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'do' lazy_block 'end'
  public static boolean doStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doStat")) return false;
    if (!nextTokenIs(b, DO)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DO_STAT, null);
    r = consumeToken(b, DO);
    p = r; // pin = 1
    r = r && report_error_(b, lazy_block(b, l + 1));
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (expr ',')* expr
  public static boolean exprList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_LIST, "<expr list>");
    r = exprList_0(b, l + 1);
    r = r && expr(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (expr ',')*
  private static boolean exprList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!exprList_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exprList_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // expr ','
  private static boolean exprList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1, -1);
    r = r && consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
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
    int c = current_position_(b);
    while (true) {
      if (!fieldList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fieldList_0_1", c)) break;
      c = current_position_(b);
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
  // 'for' paramNameDef '=' expr ',' expr (',' expr)? 'do' lazy_block 'end'
  public static boolean forAStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forAStat")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_A_STAT, null);
    r = consumeToken(b, FOR);
    r = r && paramNameDef(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 3
    r = r && report_error_(b, expr(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, COMMA)) && r;
    r = p && report_error_(b, expr(b, l + 1, -1)) && r;
    r = p && report_error_(b, forAStat_6(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, DO)) && r;
    r = p && report_error_(b, lazy_block(b, l + 1)) && r;
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
    r = r && expr(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'for' parList 'in' exprList 'do' lazy_block 'end'
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
    r = p && report_error_(b, lazy_block(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '(' (parList)? ')' lazy_block? 'end'
  public static boolean funcBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcBody")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNC_BODY, null);
    r = consumeToken(b, LPAREN);
    r = r && funcBody_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    p = r; // pin = 3
    r = r && report_error_(b, funcBody_3(b, l + 1));
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

  // lazy_block?
  private static boolean funcBody_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcBody_3")) return false;
    lazy_block(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // callOrIndexExpr <<checkType 'CALL_EXPR'>>
  static boolean funcCallExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcCallExpr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = callOrIndexExpr(b, l + 1);
    r = r && checkType(b, l + 1, CALL_EXPR);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '.' <<repeat checkFuncPrefix 1>> ID
  public static boolean funcPrefixRef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcPrefixRef")) return false;
    if (!nextTokenIs(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, INDEX_EXPR, null);
    r = consumeToken(b, DOT);
    r = r && repeat(b, l + 1, checkFuncPrefix_parser_, 1);
    r = r && consumeToken(b, ID);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'function' ID funcBody
  public static boolean globalFuncDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalFuncDef")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_FUNC_DEF, null);
    r = consumeTokens(b, 1, FUNCTION, ID);
    p = r; // pin = 1
    r = r && funcBody(b, l + 1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // 'if' expr 'then' lazy_block ('elseif' expr 'then' lazy_block)* ('else' lazy_block)? 'end'
  public static boolean ifStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_STAT, null);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, THEN)) && r;
    r = p && report_error_(b, lazy_block(b, l + 1)) && r;
    r = p && report_error_(b, ifStat_4(b, l + 1)) && r;
    r = p && report_error_(b, ifStat_5(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('elseif' expr 'then' lazy_block)*
  private static boolean ifStat_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ifStat_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifStat_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'elseif' expr 'then' lazy_block
  private static boolean ifStat_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSEIF);
    r = r && expr(b, l + 1, -1);
    r = r && consumeToken(b, THEN);
    r = r && lazy_block(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('else' lazy_block)?
  private static boolean ifStat_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat_5")) return false;
    ifStat_5_0(b, l + 1);
    return true;
  }

  // 'else' lazy_block
  private static boolean ifStat_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStat_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && lazy_block(b, l + 1);
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
    r = r && report_error_(b, expr(b, l + 1, -1));
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
  // <<lazyBlock>>
  static boolean lazy_block(PsiBuilder b, int l) {
    return lazyBlock(b, l + 1);
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
  // stat_semi*
  static boolean luaFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "luaFile")) return false;
    int c = current_position_(b);
    while (true) {
      if (!stat_semi(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "luaFile", c)) break;
      c = current_position_(b);
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
  // nameDef (',' nameDef)*
  public static boolean nameList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameList")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nameDef(b, l + 1);
    r = r && nameList_1(b, l + 1);
    exit_section_(b, m, NAME_LIST, r);
    return r;
  }

  // (',' nameDef)*
  private static boolean nameList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!nameList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "nameList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' nameDef
  private static boolean nameList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && nameDef(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // paramNameDef (',' paramNameDef)* (',' '...')? | '...'
  static boolean parList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = parList_0(b, l + 1);
    if (!r) r = consumeToken(b, ELLIPSIS);
    exit_section_(b, l, m, r, false, parList_recover_parser_);
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
    int c = current_position_(b);
    while (true) {
      if (!parList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parList_0_1", c)) break;
      c = current_position_(b);
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
    Marker m = enter_section_(b);
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, IN);
    exit_section_(b, m, null, r);
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
    r = r && report_error_(b, expr(b, l + 1, -1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // parenExpr | nameExpr | literalExpr | tableExpr
  static boolean prefixExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefixExpr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parenExpr(b, l + 1);
    if (!r) r = nameExpr(b, l + 1);
    if (!r) r = literalExpr(b, l + 1);
    if (!r) r = tableExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'repeat' lazy_block 'until' expr
  public static boolean repeatStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "repeatStat")) return false;
    if (!nextTokenIs(b, REPEAT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REPEAT_STAT, null);
    r = consumeToken(b, REPEAT);
    p = r; // pin = 1
    r = r && report_error_(b, lazy_block(b, l + 1));
    r = p && report_error_(b, consumeToken(b, UNTIL)) && r;
    r = p && expr(b, l + 1, -1) && r;
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
  // callStat |
  //     assignStat |
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
  //     uncompletedStat
  static boolean stat_impl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stat_impl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = callStat(b, l + 1);
    if (!r) r = assignStat(b, l + 1);
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
    if (!r) r = uncompletedStat(b, l + 1);
    exit_section_(b, l, m, r, false, stat_recover_parser_);
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
    Marker m = enter_section_(b);
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
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // stat_impl ';'?
  static boolean stat_semi(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stat_semi")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stat_impl(b, l + 1);
    r = r && stat_semi_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean stat_semi_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stat_semi_1")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // indexExpr | callExpr
  static boolean suffixExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "suffixExpr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = indexExpr(b, l + 1);
    if (!r) r = callExpr(b, l + 1);
    exit_section_(b, m, null, r);
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
    if (!r) r = expr(b, l + 1, -1);
    exit_section_(b, l, m, r, false, tableField_recover_parser_);
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
    r = r && report_error_(b, expr(b, l + 1, -1));
    r = p && report_error_(b, consumeTokens(b, -1, RBRACK, ASSIGN)) && r;
    r = p && expr(b, l + 1, -1) && r;
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
    r = r && expr(b, l + 1, -1);
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
    Marker m = enter_section_(b);
    r = tableFieldSep(b, l + 1);
    if (!r) r = consumeToken(b, RCURLY);
    if (!r) r = consumeToken(b, LBRACK);
    exit_section_(b, m, null, r);
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
  // expr
  public static boolean uncompletedStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uncompletedStat")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNCOMPLETED_STAT, "<uncompleted stat>");
    r = expr(b, l + 1, -1);
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // callOrIndexExpr | nameExpr
  static boolean varExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varExpr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = callOrIndexExpr(b, l + 1);
    if (!r) r = nameExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
    int c = current_position_(b);
    while (true) {
      if (!varList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varList_1", c)) break;
      c = current_position_(b);
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
  // 'while' expr 'do' lazy_block 'end'
  public static boolean whileStat(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileStat")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STAT, null);
    r = consumeToken(b, WHILE);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, DO)) && r;
    r = p && report_error_(b, lazy_block(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    register_hook_(b, LEFT_BINDER, MY_LEFT_COMMENT_BINDER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Expression root: expr
  // Operator priority table:
  // 0: BINARY(orExpr)
  // 1: BINARY(andExpr)
  // 2: BINARY(conditionalExpr)
  // 3: BINARY(bitOrExpr)
  // 4: BINARY(bitTildeExpr)
  // 5: BINARY(bitAndExpr)
  // 6: BINARY(bitMoveExpr)
  // 7: BINARY(concatExpr)
  // 8: BINARY(addExpr)
  // 9: BINARY(mulExpr)
  // 10: ATOM(unaryExpr)
  // 11: BINARY(expExpr)
  // 12: ATOM(valueExpr)
  public static boolean expr(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expr")) return false;
    addVariant(b, "<expr>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expr>");
    r = unaryExpr(b, l + 1);
    if (!r) r = valueExpr(b, l + 1);
    p = r;
    r = r && expr_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean expr_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expr_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 0 && consumeTokenSmart(b, OR)) {
        r = expr(b, l, 0);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 1 && consumeTokenSmart(b, AND)) {
        r = expr(b, l, 1);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 2 && conditionalExpr_0(b, l + 1)) {
        r = expr(b, l, 2);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 3 && consumeTokenSmart(b, BIT_OR)) {
        r = expr(b, l, 3);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 4 && consumeTokenSmart(b, BIT_TILDE)) {
        r = expr(b, l, 4);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 5 && consumeTokenSmart(b, BIT_AND)) {
        r = expr(b, l, 5);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 6 && bitMoveExpr_0(b, l + 1)) {
        r = expr(b, l, 6);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 7 && consumeTokenSmart(b, CONCAT)) {
        r = expr(b, l, 7);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 8 && addExpr_0(b, l + 1)) {
        r = expr(b, l, 8);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 9 && mulExpr_0(b, l + 1)) {
        r = expr(b, l, 9);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else if (g < 11 && consumeTokenSmart(b, EXP)) {
        r = expr(b, l, 11);
        exit_section_(b, l, m, BINARY_EXPR, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // '<'|'>'|'<='|'>='|'~='|'=='
  private static boolean conditionalExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LT);
    if (!r) r = consumeTokenSmart(b, GT);
    if (!r) r = consumeTokenSmart(b, LE);
    if (!r) r = consumeTokenSmart(b, GE);
    if (!r) r = consumeTokenSmart(b, NE);
    if (!r) r = consumeTokenSmart(b, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // '<<'|'>>'
  private static boolean bitMoveExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitMoveExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, BIT_LTLT);
    if (!r) r = consumeTokenSmart(b, BIT_RTRT);
    exit_section_(b, m, null, r);
    return r;
  }

  // '+'|'-'
  private static boolean addExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "addExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, PLUS);
    if (!r) r = consumeTokenSmart(b, MINUS);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'|'/'|'//'|'%'
  private static boolean mulExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mulExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, MULT);
    if (!r) r = consumeTokenSmart(b, DIV);
    if (!r) r = consumeTokenSmart(b, DOUBLE_DIV);
    if (!r) r = consumeTokenSmart(b, MOD);
    exit_section_(b, m, null, r);
    return r;
  }

  // unaryOp expr
  public static boolean unaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNARY_EXPR, "<unary expr>");
    r = unaryOp(b, l + 1);
    p = r; // pin = 1
    r = r && expr(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // varExpr | literalExpr | closureExpr | tableExpr | parenExpr
  public static boolean valueExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE_EXPR, "<value expr>");
    r = varExpr(b, l + 1);
    if (!r) r = literalExpr(b, l + 1);
    if (!r) r = closureExpr(b, l + 1);
    if (!r) r = tableExpr(b, l + 1);
    if (!r) r = parenExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  final static Parser checkFuncPrefix_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return checkFuncPrefix(b, l + 1);
    }
  };
  final static Parser parList_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return parList_recover(b, l + 1);
    }
  };
  final static Parser stat_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return stat_recover(b, l + 1);
    }
  };
  final static Parser tableField_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return tableField_recover(b, l + 1);
    }
  };
}
