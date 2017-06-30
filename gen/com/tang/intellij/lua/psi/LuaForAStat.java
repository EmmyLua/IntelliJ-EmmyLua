// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LuaForAStat extends LuaStatement, LuaParametersOwner, LuaLoop, LuaIndentRange {

  @NotNull
  List<LuaExpr> getExprList();

  @NotNull
  LuaParamNameDef getParamNameDef();

  @NotNull
  List<LuaParamNameDef> getParamNameDefList();

}
