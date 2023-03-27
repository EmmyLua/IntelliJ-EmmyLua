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
import com.tang.intellij.lua.ty.ITy;

public class LuaDocFunctionTyImpl extends LuaDocTyImpl implements LuaDocFunctionTy {

  public LuaDocFunctionTyImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitFunctionTy(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LuaDocFunctionParam> getFunctionParamList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaDocFunctionParam.class);
  }

  @Override
  @NotNull
  public List<LuaDocGenericDef> getGenericDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaDocGenericDef.class);
  }

  @Override
  @Nullable
  public LuaDocTypeList getTypeList() {
    return PsiTreeUtil.getChildOfType(this, LuaDocTypeList.class);
  }

  @Override
  @Nullable
  public LuaDocVarargParam getVarargParam() {
    return PsiTreeUtil.getChildOfType(this, LuaDocVarargParam.class);
  }

  @Override
  @NotNull
  public ITy getType() {
    return LuaDocPsiImplUtilKt.getType(this);
  }

  @Override
  @NotNull
  public ITy getReturnType() {
    return LuaDocPsiImplUtilKt.getReturnType(this);
  }

}
