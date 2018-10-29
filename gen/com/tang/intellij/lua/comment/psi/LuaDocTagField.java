// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
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

public interface LuaDocTagField extends LuaClassField, LuaDocPsiElement, PsiNameIdentifierOwner, LuaDocTag, StubBasedPsiElement<LuaDocFieldDefStub> {

  @Nullable
  LuaDocAccessModifier getAccessModifier();

  @Nullable
  LuaDocClassNameRef getClassNameRef();

  @Nullable
  LuaDocCommentString getCommentString();

  @Nullable
  LuaDocTy getTy();

  @Nullable
  PsiElement getId();

  @NotNull
  ITy guessParentType(@NotNull SearchContext context);

  @NotNull
  Visibility getVisibility();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @Nullable
  String getName();

  int getTextOffset();

  @Nullable
  String getFieldName();

  @NotNull
  ItemPresentation getPresentation();

  boolean isDeprecated();

}
