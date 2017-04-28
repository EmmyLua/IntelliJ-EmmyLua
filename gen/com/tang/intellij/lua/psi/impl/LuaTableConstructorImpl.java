// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.tang.intellij.lua.stubs.LuaTableStub;
import com.tang.intellij.lua.psi.*;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaTableConstructorImpl extends StubBasedPsiElementBase<LuaTableStub> implements LuaTableConstructor {

  public LuaTableConstructorImpl(LuaTableStub stub, IStubElementType type) {
    super(stub, type);
  }

  public LuaTableConstructorImpl(ASTNode node) {
    super(node);
  }

  public LuaTableConstructorImpl(LuaTableStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitTableConstructor(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaFieldList getFieldList() {
    return PsiTreeUtil.getChildOfType(this, LuaFieldList.class);
  }

  public LuaTableField findField(String fieldName) {
    return LuaPsiImplUtil.findField(this, fieldName);
  }

  public String toString() {
    return LuaPsiImplUtil.toString(this);
  }

}
