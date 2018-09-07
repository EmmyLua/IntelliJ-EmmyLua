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
import com.tang.intellij.lua.stubs.LuaDocTableFieldDefStub;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.psi.Visibility;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaDocTableFieldImpl extends StubBasedPsiElementBase<LuaDocTableFieldDefStub> implements LuaDocTableField {

  public LuaDocTableFieldImpl(@NotNull LuaDocTableFieldDefStub stub, @NotNull IStubElementType type) {
    super(stub, type);
  }

  public LuaDocTableFieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaDocTableFieldImpl(LuaDocTableFieldDefStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitTableField(this);
  }

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
  @NotNull
  public PsiElement getId() {
    return notNullChild(findChildByType(ID));
  }

  @NotNull
  public ITy guessParentType(@NotNull SearchContext context) {
    return LuaDocPsiImplUtilKt.guessParentType(this, context);
  }

  @NotNull
  public Visibility getVisibility() {
    return LuaDocPsiImplUtilKt.getVisibility(this);
  }

  @NotNull
  public PsiElement setName(@NotNull String newName) {
    return LuaDocPsiImplUtilKt.setName(this, newName);
  }

  @NotNull
  public String getName() {
    return LuaDocPsiImplUtilKt.getName(this);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaDocPsiImplUtilKt.getNameIdentifier(this);
  }

  @NotNull
  public ITy guessType(@NotNull SearchContext context) {
    return LuaDocPsiImplUtilKt.guessType(this, context);
  }

  public boolean isDeprecated() {
    return LuaDocPsiImplUtilKt.isDeprecated(this);
  }

}
