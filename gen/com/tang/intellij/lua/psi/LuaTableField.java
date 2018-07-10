// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaTableFieldStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public interface LuaTableField extends LuaClassField, PsiNameIdentifierOwner, LuaCommentOwner, StubBasedPsiElement<LuaTableFieldStub> {

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

  @Nullable
  String getFieldName();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  ITy guessParentType(SearchContext context);

  @NotNull
  Visibility getVisibility();

  boolean isDeprecated();

  @Nullable
  LuaComment getComment();

  @Nullable
  LuaExpr getIdExpr();

  @Nullable
  PsiElement getLbrack();

}
