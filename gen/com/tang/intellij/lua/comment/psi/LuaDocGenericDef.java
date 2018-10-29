// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface LuaDocGenericDef extends PsiNameIdentifierOwner, LuaDocPsiElement {

  @Nullable
  LuaDocClassNameRef getClassNameRef();

  @NotNull
  PsiElement getId();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @Nullable
  String getName();

  int getTextOffset();

}
