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
import com.tang.intellij.lua.ty.ITyClass;

public class LuaDocGenericDefImpl extends ASTWrapperPsiElement implements LuaDocGenericDef {

  public LuaDocGenericDefImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitGenericDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaDocClassRef getClassRef() {
    return PsiTreeUtil.getChildOfType(this, LuaDocClassRef.class);
  }

  @Override
  @NotNull
  public PsiElement getId() {
    return notNullChild(findChildByType(ID));
  }

  @Override
  @NotNull
  public ITyClass getType() {
    return LuaDocPsiImplUtilKt.getType(this);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaDocPsiImplUtilKt.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String newName) {
    return LuaDocPsiImplUtilKt.setName(this, newName);
  }

  @Override
  @Nullable
  public String getName() {
    return LuaDocPsiImplUtilKt.getName(this);
  }

  @Override
  public int getTextOffset() {
    return LuaDocPsiImplUtilKt.getTextOffset(this);
  }

}
