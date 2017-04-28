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

public class LuaBlockImpl extends ASTWrapperPsiElement implements LuaBlock {

  public LuaBlockImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitBlock(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LuaClassMethodDef> getClassMethodDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaClassMethodDef.class);
  }

  @Override
  @NotNull
  public List<LuaGlobalFuncDef> getGlobalFuncDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaGlobalFuncDef.class);
  }

  @Override
  @NotNull
  public List<LuaLocalDef> getLocalDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaLocalDef.class);
  }

  @Override
  @NotNull
  public List<LuaLocalFuncDef> getLocalFuncDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaLocalFuncDef.class);
  }

  @Override
  @NotNull
  public List<LuaStatement> getStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaStatement.class);
  }

}
