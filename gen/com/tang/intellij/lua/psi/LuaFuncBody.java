// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LuaFuncBody extends LuaIndentRange {

  @NotNull
  List<LuaParamNameDef> getParamNameDefList();

  @NotNull
  PsiElement getRparen();

  @Nullable
  PsiElement getEllipsis();

}
