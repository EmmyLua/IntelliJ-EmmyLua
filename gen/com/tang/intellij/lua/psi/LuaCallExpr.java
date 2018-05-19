// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaExprStub;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public interface LuaCallExpr extends LuaExpr, StubBasedPsiElement<LuaExprStub> {

  @NotNull
  LuaArgs getArgs();

  @NotNull
  LuaExpr getExpr();

  @NotNull
  ITy guessParentType(SearchContext context);

  @Nullable
  LuaFuncBodyOwner resolveFuncBodyOwner(SearchContext context);

  @Nullable
  PsiElement getFirstStringArg();

  boolean isMethodDotCall();

  boolean isMethodColonCall();

  boolean isFunctionCall();

}
