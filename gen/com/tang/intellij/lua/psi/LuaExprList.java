// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.Ty;

public interface LuaExprList extends LuaPsiElement {

  @NotNull
  List<LuaExpr> getExprList();

  @NotNull
  Ty guessTypeAt(SearchContext context);

}
