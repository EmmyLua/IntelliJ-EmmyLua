// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.Ty;

public interface LuaCallExpr extends LuaExpr {

  @NotNull
  LuaArgs getArgs();

  @NotNull
  LuaExpr getExpr();

  @NotNull
  Ty guessPrefixType(SearchContext context);

  @Nullable
  LuaFuncBodyOwner resolveFuncBodyOwner(SearchContext context);

  @Nullable
  PsiElement getFirstStringArg();

  boolean isStaticMethodCall();

  boolean isMethodCall();

  boolean isFunctionCall();

}
