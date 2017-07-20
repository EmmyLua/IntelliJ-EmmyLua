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
import com.tang.intellij.lua.stubs.LuaTableFieldStub;
import com.tang.intellij.lua.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaTableFieldImpl extends StubBasedPsiElementBase<LuaTableFieldStub> implements LuaTableField {

  public LuaTableFieldImpl(LuaTableFieldStub stub, IStubElementType type) {
    super(stub, type);
  }

  public LuaTableFieldImpl(ASTNode node) {
    super(node);
  }

  public LuaTableFieldImpl(LuaTableFieldStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitTableField(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LuaExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaExpr.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaPsiImplUtil.getNameIdentifier(this);
  }

  @NotNull
  public PsiElement setName(String name) {
    return LuaPsiImplUtil.setName(this, name);
  }

  @Nullable
  public String getName() {
    return LuaPsiImplUtil.getName(this);
  }

  public int getTextOffset() {
    return LuaPsiImplUtil.getTextOffset(this);
  }

  @NotNull
  public String toString() {
    return LuaPsiImplUtil.toString(this);
  }

  @Nullable
  public String getFieldName() {
    return LuaPsiImplUtil.getFieldName(this);
  }

  @NotNull
  public ItemPresentation getPresentation() {
    return LuaPsiImplUtil.getPresentation(this);
  }

  @Nullable
  public LuaTypeSet guessType(SearchContext context) {
    return LuaPsiImplUtil.guessType(this, context);
  }

  @Nullable
  public LuaComment getComment() {
    return LuaPsiImplUtil.getComment(this);
  }

}
