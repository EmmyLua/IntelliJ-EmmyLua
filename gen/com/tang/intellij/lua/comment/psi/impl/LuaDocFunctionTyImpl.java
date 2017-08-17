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
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.Ty;

public class LuaDocFunctionTyImpl extends LuaDocTyImpl implements LuaDocFunctionTy {

  public LuaDocFunctionTyImpl(ASTNode node) {
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
  @NotNull
  public LuaDocFunctionParam getFunctionParam() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LuaDocFunctionParam.class));
  }

  @Override
  @Nullable
  public LuaDocTypeSet getTypeSet() {
    return PsiTreeUtil.getChildOfType(this, LuaDocTypeSet.class);
  }

  @NotNull
  public Ty getType(SearchContext searchContext) {
    return LuaDocPsiImplUtilKt.getType(this, searchContext);
  }

  @NotNull
  public Ty getReturnType(SearchContext searchContext) {
    return LuaDocPsiImplUtilKt.getReturnType(this, searchContext);
  }

}
