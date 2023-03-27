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
import com.tang.intellij.lua.stubs.LuaDocTagClassStub;
import com.tang.intellij.lua.comment.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.ty.ITyClass;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaDocTagClassImpl extends StubBasedPsiElementBase<LuaDocTagClassStub> implements LuaDocTagClass {

  public LuaDocTagClassImpl(@NotNull LuaDocTagClassStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public LuaDocTagClassImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaDocTagClassImpl(LuaDocTagClassStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitTagClass(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaDocCommentString getCommentString() {
    return PsiTreeUtil.getChildOfType(this, LuaDocCommentString.class);
  }

  @Override
  @NotNull
  public PsiElement getId() {
    return notNullChild(findChildByType(ID));
  }

  @Override
  @NotNull
  public ITyClass getType() {
    return LuaDocPsiImplUtilKt.getType(this);
  }

  @Override
  @NotNull
  public ItemPresentation getPresentation() {
    return LuaDocPsiImplUtilKt.getPresentation(this);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return LuaDocPsiImplUtilKt.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String newName) {
    return LuaDocPsiImplUtilKt.setName(this, newName);
  }

  @Override
  @NotNull
  public String getName() {
    return LuaDocPsiImplUtilKt.getName(this);
  }

  @Override
  public int getTextOffset() {
    return LuaDocPsiImplUtilKt.getTextOffset(this);
  }

  @Override
  @NotNull
  public String toString() {
    return LuaDocPsiImplUtilKt.toString(this);
  }

  @Override
  public boolean isDeprecated() {
    return LuaDocPsiImplUtilKt.isDeprecated(this);
  }

  @Override
  @Nullable
  public LuaDocClassNameRef getSuperClassNameRef() {
    return PsiTreeUtil.getChildOfType(this, LuaDocClassNameRef.class);
  }

  @Override
  @Nullable
  public PsiElement getModule() {
    return findChildByType(TAG_NAME_MODULE);
  }

}
