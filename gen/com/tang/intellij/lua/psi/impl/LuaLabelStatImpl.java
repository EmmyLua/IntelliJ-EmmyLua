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

public class LuaLabelStatImpl extends LuaStatementImpl implements LuaLabelStat {

  public LuaLabelStatImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitLabelStat(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaPsiImplUtilKt.getNameIdentifier(this);
  }

  public int getTextOffset() {
    return LuaPsiImplUtilKt.getTextOffset(this);
  }

  @NotNull
  public PsiElement setName(String name) {
    return LuaPsiImplUtilKt.setName(this, name);
  }

  @Nullable
  public String getName() {
    return LuaPsiImplUtilKt.getName(this);
  }

}
