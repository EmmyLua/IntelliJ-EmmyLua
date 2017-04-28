// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.SearchScope;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public interface LuaLocalFuncDef extends LuaFuncBodyOwner, LuaDeclaration, LuaStatement, PsiNameIdentifierOwner {

  @Nullable
  LuaFuncBody getFuncBody();

  @Nullable
  PsiElement getId();

  LuaComment getComment();

  List<LuaParamNameDef> getParamNameDefList();

  PsiElement getNameIdentifier();

  PsiElement setName(String name);

  String getName();

  int getTextOffset();

  SearchScope getUseScope();

  LuaTypeSet guessReturnTypeSet(SearchContext searchContext);

  @NotNull
  LuaParamInfo[] getParams();

}
