// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaClassMethodStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public interface LuaClassMethodDef extends LuaFuncBodyOwner, LuaDeclaration, LuaClassMember, LuaStatement, PsiNameIdentifierOwner, StubBasedPsiElement<LuaClassMethodStub> {

  @NotNull
  LuaClassMethodName getClassMethodName();

  @Nullable
  LuaFuncBody getFuncBody();

  @Nullable
  LuaComment getComment();

  @Nullable
  LuaType getClassType(SearchContext context);

  @NotNull
  List<LuaParamNameDef> getParamNameDefList();

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(String name);

  @Nullable
  String getName();

  int getTextOffset();

  @Nullable
  LuaTypeSet guessReturnTypeSet(SearchContext searchContext);

  @NotNull
  LuaParamInfo[] getParams();

  boolean isStatic();

  @NotNull
  ItemPresentation getPresentation();

}
