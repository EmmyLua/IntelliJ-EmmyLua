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
import com.tang.intellij.lua.stubs.LuaFuncStub;
import com.tang.intellij.lua.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;
import com.tang.intellij.lua.ty.ITyClass;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaFuncDefImpl extends StubBasedPsiElementBase<LuaFuncStub> implements LuaFuncDef {

  public LuaFuncDefImpl(@NotNull LuaFuncStub stub, @NotNull IStubElementType type) {
    super(stub, type);
  }

  public LuaFuncDefImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaFuncDefImpl(LuaFuncStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitFuncDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaFuncBody getFuncBody() {
    return PsiTreeUtil.getStubChildOfType(this, LuaFuncBody.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @Nullable
  public LuaComment getComment() {
    return LuaPsiImplUtilKt.getComment(this);
  }

  @Override
  @NotNull
  public ItemPresentation getPresentation() {
    return LuaPsiImplUtilKt.getPresentation(this);
  }

  @Override
  @NotNull
  public List<LuaParamNameDef> getParamNameDefList() {
    return LuaPsiImplUtilKt.getParamNameDefList(this);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaPsiImplUtilKt.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String name) {
    return LuaPsiImplUtilKt.setName(this, name);
  }

  @Override
  @Nullable
  public String getName() {
    return LuaPsiImplUtilKt.getName(this);
  }

  @Override
  public int getTextOffset() {
    return LuaPsiImplUtilKt.getTextOffset(this);
  }

  @Override
  @NotNull
  public String toString() {
    return LuaPsiImplUtilKt.toString(this);
  }

  @Override
  @NotNull
  public ITy guessReturnType(@NotNull SearchContext searchContext) {
    return LuaPsiImplUtilKt.guessReturnType(this, searchContext);
  }

  @Override
  @NotNull
  public ITyClass guessParentType(@NotNull SearchContext searchContext) {
    return LuaPsiImplUtilKt.guessParentType(this, searchContext);
  }

  @Override
  @NotNull
  public Visibility getVisibility() {
    return LuaPsiImplUtilKt.getVisibility(this);
  }

  @Override
  public boolean isDeprecated() {
    return LuaPsiImplUtilKt.isDeprecated(this);
  }

  @Override
  @NotNull
  public LuaParamInfo[] getParams() {
    return LuaPsiImplUtilKt.getParams(this);
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    return LuaPsiImplUtilKt.getReferences(this);
  }

}
