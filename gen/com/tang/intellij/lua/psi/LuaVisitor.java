// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.tang.intellij.lua.stubs.LuaExprStubElement;
import com.tang.intellij.lua.stubs.LuaNameExprStub;
import com.tang.intellij.lua.stubs.LuaTableExprStub;
import com.tang.intellij.lua.stubs.LuaUnaryExprStub;
import com.tang.intellij.lua.stubs.LuaBinaryExprStub;
import com.tang.intellij.lua.stubs.LuaLiteralExprStub;
import com.tang.intellij.lua.stubs.LuaIndexExprStub;
import com.tang.intellij.lua.stubs.LuaClosureExprStub;

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
    // visitExprStubElement(o);
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

  public void visitClassMethodDef(@NotNull LuaClassMethodDef o) {
    visitClassMethod(o);
    // visitDeclaration(o);
    // visitStatement(o);
  }

  public void visitClassMethodName(@NotNull LuaClassMethodName o) {
    visitPsiElement(o);
  }

  public void visitClosureExpr(@NotNull LuaClosureExpr o) {
    visitExpr(o);
    // visitFuncBodyOwner(o);
    // visitExprStubElement(o);
  }

  public void visitDoStat(@NotNull LuaDoStat o) {
    visitStatement(o);
    // visitIndentRange(o);
  }

  public void visitEmptyStat(@NotNull LuaEmptyStat o) {
    visitStatement(o);
  }

  public void visitExpr(@NotNull LuaExpr o) {
    visitTypeGuessable(o);
  }

  public void visitExprList(@NotNull LuaExprList o) {
    visitPsiElement(o);
  }

  public void visitExprStat(@NotNull LuaExprStat o) {
    visitStatement(o);
  }

  public void visitForAStat(@NotNull LuaForAStat o) {
    visitStatement(o);
    // visitParametersOwner(o);
    // visitLoop(o);
    // visitIndentRange(o);
  }

  public void visitForBStat(@NotNull LuaForBStat o) {
    visitStatement(o);
    // visitParametersOwner(o);
    // visitLoop(o);
    // visitIndentRange(o);
  }

  public void visitFuncBody(@NotNull LuaFuncBody o) {
    visitIndentRange(o);
  }

  public void visitFuncDef(@NotNull LuaFuncDef o) {
    visitClassMethod(o);
    // visitDeclaration(o);
    // visitStatement(o);
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
    // visitClassMember(o);
    // visitExprStubElement(o);
  }

  public void visitLabelStat(@NotNull LuaLabelStat o) {
    visitStatement(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitListArgs(@NotNull LuaListArgs o) {
    visitArgs(o);
  }

  public void visitLiteralExpr(@NotNull LuaLiteralExpr o) {
    visitExpr(o);
    // visitExprStubElement(o);
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

  public void visitNameDef(@NotNull LuaNameDef o) {
    visitNamedElement(o);
    // visitTypeGuessable(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitNameExpr(@NotNull LuaNameExpr o) {
    visitExpr(o);
    // visitPsiNameIdentifierOwner(o);
    // visitExprStubElement(o);
    // visitModuleClassField(o);
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
    // visitLoop(o);
    // visitIndentRange(o);
  }

  public void visitReturnStat(@NotNull LuaReturnStat o) {
    visitStatement(o);
  }

  public void visitShebangLine(@NotNull LuaShebangLine o) {
    visitPsiElement(o);
  }

  public void visitSingleArg(@NotNull LuaSingleArg o) {
    visitArgs(o);
  }

  public void visitStatement(@NotNull LuaStatement o) {
    visitCommentOwner(o);
  }

  public void visitTableExpr(@NotNull LuaTableExpr o) {
    visitExpr(o);
    // visitIndentRange(o);
    // visitExprStubElement(o);
  }

  public void visitTableField(@NotNull LuaTableField o) {
    visitClassField(o);
    // visitPsiNameIdentifierOwner(o);
    // visitCommentOwner(o);
  }

  public void visitTableFieldSep(@NotNull LuaTableFieldSep o) {
    visitPsiElement(o);
  }

  public void visitUnaryExpr(@NotNull LuaUnaryExpr o) {
    visitExpr(o);
    // visitExprStubElement(o);
  }

  public void visitUnaryOp(@NotNull LuaUnaryOp o) {
    visitPsiElement(o);
  }

  public void visitVarList(@NotNull LuaVarList o) {
    visitExprList(o);
  }

  public void visitWhileStat(@NotNull LuaWhileStat o) {
    visitStatement(o);
    // visitLoop(o);
    // visitIndentRange(o);
  }

  public void visitClassField(@NotNull LuaClassField o) {
    visitPsiElement(o);
  }

  public void visitClassMethod(@NotNull LuaClassMethod o) {
    visitPsiElement(o);
  }

  public void visitCommentOwner(@NotNull LuaCommentOwner o) {
    visitPsiElement(o);
  }

  public void visitDeclaration(@NotNull LuaDeclaration o) {
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

  public void visitTypeGuessable(@NotNull LuaTypeGuessable o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull LuaPsiElement o) {
    visitElement(o);
  }

}
