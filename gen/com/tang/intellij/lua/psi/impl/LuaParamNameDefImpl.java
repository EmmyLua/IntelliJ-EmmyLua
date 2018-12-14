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
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.stubs.LuaNameDefStub;

public class LuaParamNameDefImpl extends LuaNameDefImpl implements LuaParamNameDef {

  public LuaParamNameDefImpl(@NotNull LuaNameDefStub stub, @NotNull IStubElementType type) {
    super(stub, type);
  }

  public LuaParamNameDefImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaParamNameDefImpl(LuaNameDefStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitParamNameDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

}
