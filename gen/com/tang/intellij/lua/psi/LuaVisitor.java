// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameIdentifierOwner;
import  com.tang.intellij.lua.psi.LuaParametersOwner;
import  com.tang.intellij.lua.psi.LuaCommentOwner;

public class LuaVisitor extends PsiElementVisitor {

  public void visitArgs(@NotNull LuaArgs o) {
    visitPsiElement(o);
  }

  public void visitAssignStat(@NotNull LuaAssignStat o) {
    visitStatement(o);
    // visitDeclaration(o);
  }

  public void visitBinaryExpr(@NotNull LuaBinaryExpr o) {
    visitExpr(o);
  }

  public void visitBinaryOp(@NotNull LuaBinaryOp o) {
    visitPsiElement(o);
  }

  public void visitBlock(@NotNull LuaBlock o) {
    visitPsiElement(o);
  }

  public void visitBreakStat(@NotNull LuaBreakStat o) {
    visitStatement(o);
  }

  public void visitCallExpr(@NotNull LuaCallExpr o) {
    visitExpr(o);
  }

  public void visitCallStat(@NotNull LuaCallStat o) {
    visitExprStat(o);
  }

  public void visitClassMethodDef(@NotNull LuaClassMethodDef o) {
    visitFuncBodyOwner(o);
    // visitDeclaration(o);
    // visitClassMember(o);
    // visitStatement(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitClassMethodName(@NotNull LuaClassMethodName o) {
    visitPsiElement(o);
  }

  public void visitClosureExpr(@NotNull LuaClosureExpr o) {
    visitExpr(o);
    // visitFuncBodyOwner(o);
  }

  public void visitDoStat(@NotNull LuaDoStat o) {
    visitStatement(o);
    // visitIndentRange(o);
  }

  public void visitExpr(@NotNull LuaExpr o) {
    visitExpression(o);
  }

  public void visitExprList(@NotNull LuaExprList o) {
    visitPsiElement(o);
  }

  public void visitExprStat(@NotNull LuaExprStat o) {
    visitStatement(o);
  }

  public void visitFieldList(@NotNull LuaFieldList o) {
    visitPsiElement(o);
  }

  public void visitForAStat(@NotNull LuaForAStat o) {
    visitStatement(o);
    // visitIndentRange(o);
  }

  public void visitForBStat(@NotNull LuaForBStat o) {
    visitStatement(o);
    // visitIndentRange(o);
  }

  public void visitFuncBody(@NotNull LuaFuncBody o) {
    visitIndentRange(o);
  }

  public void visitGlobalFuncDef(@NotNull LuaGlobalFuncDef o) {
    visitFuncBodyOwner(o);
    // visitDeclaration(o);
    // visitStatement(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitGotoStat(@NotNull LuaGotoStat o) {
    visitStatement(o);
  }

  public void visitIfStat(@NotNull LuaIfStat o) {
    visitStatement(o);
    // visitIndentRange(o);
  }

  public void visitIndexExpr(@NotNull LuaIndexExpr o) {
    visitExpr(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitLabelStat(@NotNull LuaLabelStat o) {
    visitStatement(o);
  }

  public void visitLiteralExpr(@NotNull LuaLiteralExpr o) {
    visitExpr(o);
  }

  public void visitLocalDef(@NotNull LuaLocalDef o) {
    visitDeclaration(o);
    // visitStatement(o);
  }

  public void visitLocalFuncDef(@NotNull LuaLocalFuncDef o) {
    visitFuncBodyOwner(o);
    // visitDeclaration(o);
    // visitStatement(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitName(@NotNull LuaName o) {
    visitNamedElement(o);
  }

  public void visitNameDef(@NotNull LuaNameDef o) {
    visitName(o);
    // visitTypeGuessable(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitNameExpr(@NotNull LuaNameExpr o) {
    visitExpr(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitNameList(@NotNull LuaNameList o) {
    visitPsiElement(o);
  }

  public void visitParamNameDef(@NotNull LuaParamNameDef o) {
    visitNameDef(o);
  }

  public void visitParenExpr(@NotNull LuaParenExpr o) {
    visitExpr(o);
  }

  public void visitRepeatStat(@NotNull LuaRepeatStat o) {
    visitStatement(o);
    // visitIndentRange(o);
  }

  public void visitReturnStat(@NotNull LuaReturnStat o) {
    visitStatement(o);
  }

  public void visitStatement(@NotNull LuaStatement o) {
    visitCommentOwner(o);
  }

  public void visitTableExpr(@NotNull LuaTableExpr o) {
    visitExpr(o);
    // visitIndentRange(o);
  }

  public void visitTableField(@NotNull LuaTableField o) {
    visitClassField(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitTableFieldSep(@NotNull LuaTableFieldSep o) {
    visitPsiElement(o);
  }

  public void visitUnaryExpr(@NotNull LuaUnaryExpr o) {
    visitExpr(o);
  }

  public void visitUnaryOp(@NotNull LuaUnaryOp o) {
    visitPsiElement(o);
  }

  public void visitUncompletedStat(@NotNull LuaUncompletedStat o) {
    visitExprStat(o);
  }

  public void visitValueExpr(@NotNull LuaValueExpr o) {
    visitExpr(o);
  }

  public void visitVarList(@NotNull LuaVarList o) {
    visitExprList(o);
  }

  public void visitWhileStat(@NotNull LuaWhileStat o) {
    visitStatement(o);
    // visitIndentRange(o);
  }

  public void visitClassField(@NotNull LuaClassField o) {
    visitPsiElement(o);
  }

  public void visitCommentOwner(@NotNull LuaCommentOwner o) {
    visitPsiElement(o);
  }

  public void visitDeclaration(@NotNull LuaDeclaration o) {
    visitPsiElement(o);
  }

  public void visitExpression(@NotNull LuaExpression o) {
    visitPsiElement(o);
  }

  public void visitFuncBodyOwner(@NotNull LuaFuncBodyOwner o) {
    visitPsiElement(o);
  }

  public void visitIndentRange(@NotNull LuaIndentRange o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull LuaNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull LuaPsiElement o) {
    visitElement(o);
  }

}
