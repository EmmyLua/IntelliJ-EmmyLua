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
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.tang.intellij.lua.stubs.LuaNameStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaNameExprImpl extends LuaNameExpressionImpl implements LuaNameExpr {

  public LuaNameExprImpl(LuaNameStub stub, IStubElementType type) {
    super(stub, type);
  }

  public LuaNameExprImpl(ASTNode node) {
    super(node);
  }

  public LuaNameExprImpl(LuaNameStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitNameExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getId() {
    return notNullChild(findChildByType(ID));
  }

  public PsiElement setName(String name) {
    return LuaPsiImplUtil.setName(this, name);
  }

  @NotNull
  public String getName() {
    return LuaPsiImplUtil.getName(this);
  }

  @NotNull
  public PsiElement getNameIdentifier() {
    return LuaPsiImplUtil.getNameIdentifier(this);
  }

  public ItemPresentation getPresentation() {
    return LuaPsiImplUtil.getPresentation(this);
  }

  public PsiReference[] getReferences() {
    return LuaPsiImplUtil.getReferences(this);
  }

  public String toString() {
    return LuaPsiImplUtil.toString(this);
  }

}
