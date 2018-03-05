// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaClassField;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.tang.intellij.lua.psi.Visibility;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public interface LuaDocTableField extends LuaClassField, PsiNameIdentifierOwner {

  @Nullable
  LuaDocTy getTy();

  @NotNull
  PsiElement getId();

  @NotNull
  ITy guessParentType(SearchContext context);

  @NotNull
  Visibility getVisibility();

  @NotNull
  PsiElement setName(String newName);

  @Nullable
  String getName();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  ITy guessType(SearchContext context);

}
