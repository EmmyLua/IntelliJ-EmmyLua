// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.comment.psi.LuaDocTypes.*;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.tang.intellij.lua.stubs.LuaDocTableDefStub;
import com.tang.intellij.lua.comment.psi.*;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaDocTableDefImpl extends StubBasedPsiElementBase<LuaDocTableDefStub> implements LuaDocTableDef {

  public LuaDocTableDefImpl(@NotNull LuaDocTableDefStub stub, @NotNull IStubElementType type) {
    super(stub, type);
  }

  public LuaDocTableDefImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaDocTableDefImpl(LuaDocTableDefStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitTableDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LuaDocTableField> getTableFieldList() {
    return PsiTreeUtil.getStubChildrenOfTypeAsList(this, LuaDocTableField.class);
  }

}
