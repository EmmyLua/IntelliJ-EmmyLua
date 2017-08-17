// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.Ty;

public interface LuaClosureExpr extends LuaExpr, LuaFuncBodyOwner {

  @NotNull
  LuaFuncBody getFuncBody();

  @NotNull
  List<LuaParamNameDef> getParamNameDefList();

  @NotNull
  Ty guessReturnTypeSet(SearchContext searchContext);

  @NotNull
  LuaParamInfo[] getParams();

}
