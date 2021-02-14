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
import com.tang.intellij.lua.stubs.LuaTableExprStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.stubs.LuaExprStub;

public class LuaTableExprImpl extends LuaTableExprMixin implements LuaTableExpr {

  public LuaTableExprImpl(@NotNull LuaTableExprStub stub, @NotNull IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public LuaTableExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaTableExprImpl(@NotNull LuaTableExprStub stub, @NotNull IElementType type, @NotNull ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitTableExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LuaTableField> getTableFieldList() {
    return PsiTreeUtil.getStubChildrenOfTypeAsList(this, LuaTableField.class);
  }

  @Override
  @NotNull
  public List<LuaTableFieldSep> getTableFieldSepList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaTableFieldSep.class);
  }

  @Override
  @Nullable
  public LuaTableField findField(@NotNull String fieldName) {
    return LuaPsiImplUtilKt.findField(this, fieldName);
  }

}
