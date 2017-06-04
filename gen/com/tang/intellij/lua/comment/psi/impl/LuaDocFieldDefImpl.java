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

  public LuaTypeSet guessType(SearchContext context) {
    return LuaDocPsiImplUtil.guessType(this, context);
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

  public String getFieldName() {
    return LuaDocPsiImplUtil.getFieldName(this);
  }

  public String toString() {
    return LuaDocPsiImplUtil.toString(this);
  }

}
