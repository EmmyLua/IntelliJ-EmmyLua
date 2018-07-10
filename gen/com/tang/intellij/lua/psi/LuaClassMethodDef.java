// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaClassMethodStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public interface LuaClassMethodDef extends LuaClassMethod, LuaDeclaration, LuaStatement, StubBasedPsiElement<LuaClassMethodStub> {

  @NotNull
  LuaClassMethodName getClassMethodName();

  @Nullable
  LuaFuncBody getFuncBody();

  @Nullable
  LuaComment getComment();

  @NotNull
  ITy guessParentType(SearchContext context);

  @NotNull
  Visibility getVisibility();

  boolean isDeprecated();

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
  ITy guessReturnType(SearchContext searchContext);

  @NotNull
  LuaParamInfo[] getParams();

  boolean isStatic();

  @NotNull
  ItemPresentation getPresentation();

}
