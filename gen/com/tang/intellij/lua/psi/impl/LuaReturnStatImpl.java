// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.tang.intellij.lua.stubs.LuaPlaceholderStub;
import com.tang.intellij.lua.psi.*;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaReturnStatImpl extends LuaStatMixin<LuaPlaceholderStub> implements LuaReturnStat {

  public LuaReturnStatImpl(@NotNull LuaPlaceholderStub stub, @NotNull IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public LuaReturnStatImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaReturnStatImpl(@NotNull LuaPlaceholderStub stub, @NotNull IElementType type, @NotNull ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitReturnStat(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaExprList getExprList() {
    return PsiTreeUtil.getStubChildOfType(this, LuaExprList.class);
  }

}
