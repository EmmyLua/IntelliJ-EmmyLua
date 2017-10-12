// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;

public class LuaCallExprImpl extends LuaExprImpl implements LuaCallExpr {

  public LuaCallExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitCallExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public LuaArgs getArgs() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LuaArgs.class));
  }

  @Override
  @NotNull
  public LuaExpr getExpr() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LuaExpr.class));
  }

  @NotNull
  public ITy guessParentType(SearchContext context) {
    return LuaPsiImplUtilKt.guessParentType(this, context);
  }

  @Nullable
  public LuaFuncBodyOwner resolveFuncBodyOwner(SearchContext context) {
    return LuaPsiImplUtilKt.resolveFuncBodyOwner(this, context);
  }

  @Nullable
  public PsiElement getFirstStringArg() {
    return LuaPsiImplUtilKt.getFirstStringArg(this);
  }

  public boolean isStaticMethodCall() {
    return LuaPsiImplUtilKt.isStaticMethodCall(this);
  }

  public boolean isMethodCall() {
    return LuaPsiImplUtilKt.isMethodCall(this);
  }

  public boolean isFunctionCall() {
    return LuaPsiImplUtilKt.isFunctionCall(this);
  }

}
