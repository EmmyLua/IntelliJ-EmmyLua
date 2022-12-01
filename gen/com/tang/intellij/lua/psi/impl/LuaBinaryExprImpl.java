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
import com.tang.intellij.lua.stubs.LuaBinaryExprStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.stubs.LuaExprStub;

public class LuaBinaryExprImpl extends LuaBinaryExprMixin implements LuaBinaryExpr {

  public LuaBinaryExprImpl(@NotNull LuaBinaryExprStub stub, @NotNull IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public LuaBinaryExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaBinaryExprImpl(@NotNull LuaBinaryExprStub stub, @NotNull IElementType type, @NotNull ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitBinaryExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public LuaBinaryOp getBinaryOp() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LuaBinaryOp.class));
  }

  @Override
  @Nullable
  public LuaExpr getExpr() {
    return PsiTreeUtil.getStubChildOfType(this, LuaExpr.class);
  }

}
