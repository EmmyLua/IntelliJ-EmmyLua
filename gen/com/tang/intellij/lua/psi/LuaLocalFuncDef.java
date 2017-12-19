// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaLocalFuncDefStub;
import com.intellij.psi.search.SearchScope;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public interface LuaLocalFuncDef extends LuaFuncBodyOwner, LuaDeclaration, LuaStatement, PsiNameIdentifierOwner, StubBasedPsiElement<LuaLocalFuncDefStub> {

  @Nullable
  LuaFuncBody getFuncBody();

  @Nullable
  PsiElement getId();

  @Nullable
  LuaComment getComment();

  @NotNull
  List<LuaParamNameDef> getParamNameDefList();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(String name);

  @Nullable
  String getName();

  int getTextOffset();

  @NotNull
  SearchScope getUseScope();

  @NotNull
  ITy guessReturnType(SearchContext searchContext);

  @NotNull
  LuaParamInfo[] getParams();

}
