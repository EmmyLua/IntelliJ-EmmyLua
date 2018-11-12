// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaClassField;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocTableFieldDefStub;
import com.tang.intellij.lua.psi.Visibility;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public interface LuaDocTableField extends LuaDocPsiElement, LuaClassField, PsiNameIdentifierOwner, StubBasedPsiElement<LuaDocTableFieldDefStub> {

  @Nullable
  LuaDocTy getTy();

  @NotNull
  PsiElement getId();

  @NotNull
  ITy guessParentType(@NotNull SearchContext context);

  @NotNull
  Visibility getVisibility();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @NotNull
  String getName();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  ITy guessType(@NotNull SearchContext context);

  boolean isDeprecated();

}
