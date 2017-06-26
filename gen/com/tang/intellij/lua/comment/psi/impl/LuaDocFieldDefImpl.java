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
import com.tang.intellij.lua.stubs.LuaDocClassFieldStub;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaDocFieldDefImpl extends StubBasedPsiElementBase<LuaDocClassFieldStub> implements LuaDocFieldDef {

  public LuaDocFieldDefImpl(LuaDocClassFieldStub stub, IStubElementType type) {
    super(stub, type);
  }

  public LuaDocFieldDefImpl(ASTNode node) {
    super(node);
  }

  public LuaDocFieldDefImpl(LuaDocClassFieldStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitFieldDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaDocAccessModifier getAccessModifier() {
    return PsiTreeUtil.getChildOfType(this, LuaDocAccessModifier.class);
  }

  @Override
  @Nullable
  public LuaDocCommentString getCommentString() {
    return PsiTreeUtil.getChildOfType(this, LuaDocCommentString.class);
  }

  @Override
  @Nullable
  public LuaDocTypeSet getTypeSet() {
    return PsiTreeUtil.getChildOfType(this, LuaDocTypeSet.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Nullable
  public LuaTypeSet guessType(SearchContext context) {
    return LuaDocPsiImplUtilKt.guessType(this, context);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaDocPsiImplUtilKt.getNameIdentifier(this);
  }

  @NotNull
  public PsiElement setName(String newName) {
    return LuaDocPsiImplUtilKt.setName(this, newName);
  }

  @Nullable
  public String getName() {
    return LuaDocPsiImplUtilKt.getName(this);
  }

  public int getTextOffset() {
    return LuaDocPsiImplUtilKt.getTextOffset(this);
  }

  @Nullable
  public String getFieldName() {
    return LuaDocPsiImplUtilKt.getFieldName(this);
  }

  @NotNull
  public String toString() {
    return LuaDocPsiImplUtilKt.toString(this);
  }

}
