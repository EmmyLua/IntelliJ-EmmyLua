// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.comment.psi.LuaDocTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public class LuaDocParamDefImpl extends ASTWrapperPsiElement implements LuaDocParamDef {

  public LuaDocParamDefImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitParamDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaDocParamNameRef getParamNameRef() {
    return PsiTreeUtil.getChildOfType(this, LuaDocParamNameRef.class);
  }

  @Override
  @Nullable
  public LuaDocTypeSet getTypeSet() {
    return PsiTreeUtil.getChildOfType(this, LuaDocTypeSet.class);
  }

  public LuaTypeSet guessType(SearchContext context) {
    return LuaDocPsiImplUtil.guessType(this, context);
  }

  @Override
  @Nullable
  public PsiElement getOptional() {
    return findChildByType(OPTIONAL);
  }

}
