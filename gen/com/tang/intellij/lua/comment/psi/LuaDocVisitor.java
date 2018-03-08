// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.tang.intellij.lua.psi.LuaClassField;
import com.intellij.psi.PsiNameIdentifierOwner;

public class LuaDocVisitor extends PsiElementVisitor {

  public void visitAccessModifier(@NotNull LuaDocAccessModifier o) {
    visitPsiElement(o);
  }

  public void visitArrTy(@NotNull LuaDocArrTy o) {
    visitTy(o);
  }

  public void visitClassDef(@NotNull LuaDocClassDef o) {
    visitPsiElement(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitClassNameRef(@NotNull LuaDocClassNameRef o) {
    visitPsiElement(o);
  }

  public void visitCommentString(@NotNull LuaDocCommentString o) {
    visitPsiElement(o);
  }

  public void visitFieldDef(@NotNull LuaDocFieldDef o) {
    visitLuaClassField(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitFunctionParam(@NotNull LuaDocFunctionParam o) {
    visitPsiElement(o);
  }

  public void visitFunctionTy(@NotNull LuaDocFunctionTy o) {
    visitTy(o);
  }

  public void visitGeneralTy(@NotNull LuaDocGeneralTy o) {
    visitTy(o);
  }

  public void visitGenericDef(@NotNull LuaDocGenericDef o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitGenericTy(@NotNull LuaDocGenericTy o) {
    visitTy(o);
  }

  public void visitLanDef(@NotNull LuaDocLanDef o) {
    visitPsiElement(o);
  }

  public void visitOverloadDef(@NotNull LuaDocOverloadDef o) {
    visitPsiElement(o);
  }

  public void visitParTy(@NotNull LuaDocParTy o) {
    visitTy(o);
  }

  public void visitParamDef(@NotNull LuaDocParamDef o) {
    visitPsiElement(o);
  }

  public void visitParamNameRef(@NotNull LuaDocParamNameRef o) {
    visitPsiElement(o);
  }

  public void visitReturnDef(@NotNull LuaDocReturnDef o) {
    visitPsiElement(o);
  }

  public void visitSeeRefTag(@NotNull LuaDocSeeRefTag o) {
    visitPsiElement(o);
  }

  public void visitTableField(@NotNull LuaDocTableField o) {
    visitPsiElement(o);
    // visitLuaClassField(o);
    // visitPsiNameIdentifierOwner(o);
  }

  public void visitTableDef(@NotNull LuaDocTableDef o) {
    visitPsiElement(o);
  }

  public void visitTableTy(@NotNull LuaDocTableTy o) {
    visitTy(o);
  }

  public void visitTagDef(@NotNull LuaDocTagDef o) {
    visitPsiElement(o);
  }

  public void visitTagValue(@NotNull LuaDocTagValue o) {
    visitPsiElement(o);
  }

  public void visitTy(@NotNull LuaDocTy o) {
    visitType(o);
  }

  public void visitTypeDef(@NotNull LuaDocTypeDef o) {
    visitPsiElement(o);
  }

  public void visitTypeList(@NotNull LuaDocTypeList o) {
    visitPsiElement(o);
  }

  public void visitUnionTy(@NotNull LuaDocUnionTy o) {
    visitTy(o);
  }

  public void visitLuaClassField(@NotNull LuaClassField o) {
    visitElement(o);
  }

  public void visitType(@NotNull LuaDocType o) {
    visitPsiElement(o);
  }

  public void visitPsiNameIdentifierOwner(@NotNull PsiNameIdentifierOwner o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull LuaDocPsiElement o) {
    visitElement(o);
  }

}
