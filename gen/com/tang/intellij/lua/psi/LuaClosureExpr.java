// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public interface LuaClosureExpr extends LuaExpr, LuaFuncBodyOwner {

  @NotNull
  LuaFuncBody getFuncBody();

  @NotNull
  List<LuaParamNameDef> getParamNameDefList();

  @Nullable
  LuaTypeSet guessReturnTypeSet(SearchContext searchContext);

  @NotNull
  LuaParamInfo[] getParams();

}
