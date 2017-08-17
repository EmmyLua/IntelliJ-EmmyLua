// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaIndexStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.Ty;

public interface LuaIndexExpr extends LuaExpr, PsiNameIdentifierOwner, StubBasedPsiElement<LuaIndexStub> {

  @NotNull
  List<LuaExpr> getExprList();

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(String name);

  @Nullable
  String getName();

  int getTextOffset();

  @NotNull
  ItemPresentation getPresentation();

  @Nullable
  LuaLiteralExpr getIdExpr();

  @NotNull
  Ty guessPrefixType(SearchContext context);

  @NotNull
  Ty guessValueType(SearchContext context);

  @Nullable
  PsiElement getDot();

  @Nullable
  PsiElement getColon();

  @Nullable
  PsiElement getLbrack();

}
