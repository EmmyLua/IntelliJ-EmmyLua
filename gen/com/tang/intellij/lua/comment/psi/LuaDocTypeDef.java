// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public interface LuaDocTypeDef extends LuaDocPsiElement {

  @Nullable
  LuaDocTypeSet getTypeSet();

  LuaTypeSet guessType(SearchContext context);

}
