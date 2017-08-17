// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.tang.intellij.lua.psi.*;
import com.intellij.psi.search.SearchScope;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.Ty;

public class LuaLocalFuncDefImpl extends ASTWrapperPsiElement implements LuaLocalFuncDef {

  public LuaLocalFuncDefImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitLocalFuncDef(this);
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
  public SearchScope getUseScope() {
    return LuaPsiImplUtil.getUseScope(this);
  }

  @NotNull
  public Ty guessReturnTypeSet(SearchContext searchContext) {
    return LuaPsiImplUtil.guessReturnTypeSet(this, searchContext);
  }

  @NotNull
  public LuaParamInfo[] getParams() {
    return LuaPsiImplUtil.getParams(this);
  }

}
