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
import com.tang.intellij.lua.stubs.LuaGlobalFuncStub;
import com.tang.intellij.lua.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaGlobalFuncDefImpl extends StubBasedPsiElementBase<LuaGlobalFuncStub> implements LuaGlobalFuncDef {

  public LuaGlobalFuncDefImpl(LuaGlobalFuncStub stub, IStubElementType type) {
    super(stub, type);
  }

  public LuaGlobalFuncDefImpl(ASTNode node) {
    super(node);
  }

  public LuaGlobalFuncDefImpl(LuaGlobalFuncStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitGlobalFuncDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaFuncBody getFuncBody() {
    return PsiTreeUtil.getChildOfType(this, LuaFuncBody.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Nullable
  public LuaComment getComment() {
    return LuaPsiImplUtil.getComment(this);
  }

  @NotNull
  public ItemPresentation getPresentation() {
    return LuaPsiImplUtil.getPresentation(this);
  }

  @NotNull
  public List<LuaParamNameDef> getParamNameDefList() {
    return LuaPsiImplUtil.getParamNameDefList(this);
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
  public LuaTypeSet guessReturnTypeSet(SearchContext searchContext) {
    return LuaPsiImplUtil.guessReturnTypeSet(this, searchContext);
  }

  @NotNull
  public LuaParamInfo[] getParams() {
    return LuaPsiImplUtil.getParams(this);
  }

}
