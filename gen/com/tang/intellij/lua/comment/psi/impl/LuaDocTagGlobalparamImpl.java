// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.comment.psi.LuaDocTypes.*;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.tang.intellij.lua.stubs.LuaDocTagGlobalparamStub;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.ty.ITy;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaDocTagGlobalparamImpl extends StubBasedPsiElementBase<LuaDocTagGlobalparamStub> implements LuaDocTagGlobalparam {

  public LuaDocTagGlobalparamImpl(@NotNull LuaDocTagGlobalparamStub stub, @NotNull IStubElementType type) {
    super(stub, type);
  }

  public LuaDocTagGlobalparamImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaDocTagGlobalparamImpl(LuaDocTagGlobalparamStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitTagGlobalparam(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaDocTy getTy() {
    return PsiTreeUtil.getChildOfType(this, LuaDocTy.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @NotNull
  public ITy getType() {
    return LuaDocPsiImplUtilKt.getType(this);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaDocPsiImplUtilKt.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String newName) {
    return LuaDocPsiImplUtilKt.setName(this, newName);
  }

  @Override
  @Nullable
  public String getName() {
    return LuaDocPsiImplUtilKt.getName(this);
  }

}
