// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.comment.psi.LuaDocTypes.*;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.psi.LuaParamInfo;
import com.tang.intellij.lua.ty.ITy;

public class LuaDocFunctionTyImpl extends LuaDocTyImpl implements LuaDocFunctionTy {

  public LuaDocFunctionTyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitFunctionTy(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaDocFunctionParams getFunctionParams() {
    return PsiTreeUtil.getChildOfType(this, LuaDocFunctionParams.class);
  }

  @Override
  @Nullable
  public LuaDocFunctionReturnList getFunctionReturnList() {
    return PsiTreeUtil.getChildOfType(this, LuaDocFunctionReturnList.class);
  }

  @Override
  @NotNull
  public List<LuaDocGenericDef> getGenericDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaDocGenericDef.class);
  }

  @Override
  @NotNull
  public ITy getType() {
    return LuaDocPsiImplUtilKt.getType(this);
  }

  @Override
  @Nullable
  public LuaParamInfo[] getParams() {
    return LuaDocPsiImplUtilKt.getParams(this);
  }

  @Override
  @Nullable
  public ITy getVarargParam() {
    return LuaDocPsiImplUtilKt.getVarargParam(this);
  }

  @Override
  @Nullable
  public ITy getReturnType() {
    return LuaDocPsiImplUtilKt.getReturnType(this);
  }

}
