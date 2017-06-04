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
import com.tang.intellij.lua.stubs.LuaDocClassStub;
import com.tang.intellij.lua.comment.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.lang.type.LuaType;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaDocClassDefImpl extends StubBasedPsiElementBase<LuaDocClassStub> implements LuaDocClassDef {

  public LuaDocClassDefImpl(LuaDocClassStub stub, IStubElementType type) {
    super(stub, type);
  }

  public LuaDocClassDefImpl(ASTNode node) {
    super(node);
  }

  public LuaDocClassDefImpl(LuaDocClassStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitClassDef(this);
  }

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

  public LuaType getClassType() {
    return LuaDocPsiImplUtil.getClassType(this);
  }

  public ItemPresentation getPresentation() {
    return LuaDocPsiImplUtil.getPresentation(this);
  }

  public PsiElement getNameIdentifier() {
    return LuaDocPsiImplUtil.getNameIdentifier(this);
  }

  public PsiElement setName(String newName) {
    return LuaDocPsiImplUtil.setName(this, newName);
  }

  public String getName() {
    return LuaDocPsiImplUtil.getName(this);
  }

  public int getTextOffset() {
    return LuaDocPsiImplUtil.getTextOffset(this);
  }

  public String toString() {
    return LuaDocPsiImplUtil.toString(this);
  }

  @Override
  @Nullable
  public LuaDocClassNameRef getSuperClassNameRef() {
    return PsiTreeUtil.getChildOfType(this, LuaDocClassNameRef.class);
  }

}
