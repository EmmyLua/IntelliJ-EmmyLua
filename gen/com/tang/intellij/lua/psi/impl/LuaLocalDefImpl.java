// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.tang.intellij.lua.stubs.LuaPlaceholderStub;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaLocalDefImpl extends StubBasedPsiElementBase<LuaPlaceholderStub> implements LuaLocalDef {

  public LuaLocalDefImpl(LuaPlaceholderStub stub, IStubElementType type) {
    super(stub, type);
  }

  public LuaLocalDefImpl(ASTNode node) {
    super(node);
  }

  public LuaLocalDefImpl(LuaPlaceholderStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitLocalDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaExprList getExprList() {
    return PsiTreeUtil.getStubChildOfType(this, LuaExprList.class);
  }

  @Override
  @Nullable
  public LuaNameList getNameList() {
    return PsiTreeUtil.getStubChildOfType(this, LuaNameList.class);
  }

  @Nullable
  public LuaComment getComment() {
    return LuaPsiImplUtilKt.getComment(this);
  }

  @Override
  @Nullable
  public PsiElement getAssign() {
    return findChildByType(ASSIGN);
  }

}
