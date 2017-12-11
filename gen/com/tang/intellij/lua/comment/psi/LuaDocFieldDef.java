// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaClassField;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocFieldDefStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.psi.Visibility;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public interface LuaDocFieldDef extends LuaClassField, LuaDocPsiElement, PsiNameIdentifierOwner, StubBasedPsiElement<LuaDocFieldDefStub> {

  @Nullable
  LuaDocAccessModifier getAccessModifier();

  @Nullable
  LuaDocCommentString getCommentString();

  @Nullable
  LuaDocTy getTy();

  @Nullable
  PsiElement getId();

  @NotNull
  ITy guessType(SearchContext context);

  @NotNull
  ITy guessParentType(SearchContext context);

  @NotNull
  Visibility getVisibility();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(String newName);

  @Nullable
  String getName();

  int getTextOffset();

  @Nullable
  String getFieldName();

  @NotNull
  ItemPresentation getPresentation();

}
