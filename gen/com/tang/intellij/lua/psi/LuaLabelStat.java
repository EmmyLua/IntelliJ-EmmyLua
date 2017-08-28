// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface LuaLabelStat extends LuaStatement, PsiNameIdentifierOwner {

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

  @NotNull
  PsiElement setName(String name);

  @Nullable
  String getName();

}
