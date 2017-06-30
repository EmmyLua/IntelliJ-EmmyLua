// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public interface LuaExprList extends LuaPsiElement {

  @NotNull
  List<LuaExpr> getExprList();

  @Nullable
  LuaTypeSet guessTypeAt(int index, SearchContext context);

}
