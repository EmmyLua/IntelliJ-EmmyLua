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
import com.tang.intellij.lua.comment.psi.api.LuaComment;

public class LuaLocalDefImpl extends ASTWrapperPsiElement implements LuaLocalDef {

  public LuaLocalDefImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitLocalDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaExprList getExprList() {
    return PsiTreeUtil.getChildOfType(this, LuaExprList.class);
  }

  @Override
  @Nullable
  public LuaNameList getNameList() {
    return PsiTreeUtil.getChildOfType(this, LuaNameList.class);
  }

  @Nullable
  public LuaComment getComment() {
    return LuaPsiImplUtil.getComment(this);
  }

  @Override
  @Nullable
  public PsiElement getAssign() {
    return findChildByType(ASSIGN);
  }

}
