// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.comment.psi.api.LuaComment;

public class LuaAssignStatImpl extends LuaStatementImpl implements LuaAssignStat {

  public LuaAssignStatImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitAssignStat(this);
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
  @NotNull
  public LuaVarList getVarList() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LuaVarList.class));
  }

  public LuaComment getComment() {
    return LuaPsiImplUtil.getComment(this);
  }

  @Override
  @NotNull
  public PsiElement getAssign() {
    return notNullChild(findChildByType(ASSIGN));
  }

}
