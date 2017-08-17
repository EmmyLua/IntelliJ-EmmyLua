// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.Ty;

public interface LuaDocGeneralTy extends LuaDocTy {

  @NotNull
  LuaDocClassNameRef getClassNameRef();

  @NotNull
  Ty getType(SearchContext searchContext);

}
