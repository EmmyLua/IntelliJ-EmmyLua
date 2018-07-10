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
import com.tang.intellij.lua.stubs.LuaNameExprStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.stubs.LuaExprStub;

public class LuaNameExprImpl extends LuaNameExprMixin implements LuaNameExpr {

  public LuaNameExprImpl(LuaNameExprStub stub, IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public LuaNameExprImpl(ASTNode node) {
    super(node);
  }

  public LuaNameExprImpl(LuaNameExprStub stub, IElementType type, ASTNode node) {
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

  @NotNull
  public PsiElement setName(String name) {
    return LuaPsiImplUtilKt.setName(this, name);
  }

  @NotNull
  public String getName() {
    return LuaPsiImplUtilKt.getName(this);
  }

  @NotNull
  public PsiElement getNameIdentifier() {
    return LuaPsiImplUtilKt.getNameIdentifier(this);
  }

  @NotNull
  public ItemPresentation getPresentation() {
    return LuaPsiImplUtilKt.getPresentation(this);
  }

  @NotNull
  public PsiReference[] getReferences() {
    return LuaPsiImplUtilKt.getReferences(this);
  }

  public boolean isDeprecated() {
    return LuaPsiImplUtilKt.isDeprecated(this);
  }

}
