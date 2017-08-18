// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public interface LuaDocFunctionTy extends LuaDocTy {

  @NotNull
  List<LuaDocFunctionParam> getFunctionParamList();

  @Nullable
  LuaDocTypeSet getTypeSet();

  @NotNull
  ITy getType(SearchContext searchContext);

  @NotNull
  ITy getReturnType(SearchContext searchContext);

}
