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
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaIndexStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaIndexExprImpl extends LuaIndexExpressionImpl implements LuaIndexExpr {

  public LuaIndexExprImpl(LuaIndexStub stub, IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public LuaIndexExprImpl(ASTNode node) {
    super(node);
  }

  public LuaIndexExprImpl(LuaIndexStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitIndexExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LuaExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LuaExpr.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaPsiImplUtil.getNameIdentifier(this);
  }

  public PsiElement setName(String name) {
    return LuaPsiImplUtil.setName(this, name);
  }

  public String getName() {
    return LuaPsiImplUtil.getName(this);
  }

  public int getTextOffset() {
    return LuaPsiImplUtil.getTextOffset(this);
  }

  public ItemPresentation getPresentation() {
    return LuaPsiImplUtil.getPresentation(this);
  }

  public String toString() {
    return LuaPsiImplUtil.toString(this);
  }

  @Nullable
  public LuaTypeSet guessPrefixType(SearchContext context) {
    return LuaPsiImplUtil.guessPrefixType(this, context);
  }

  @Nullable
  public LuaTypeSet guessValueType(SearchContext context) {
    return LuaPsiImplUtil.guessValueType(this, context);
  }

  @Override
  @Nullable
  public PsiElement getDot() {
    return findChildByType(DOT);
  }

  @Override
  @Nullable
  public PsiElement getColon() {
    return findChildByType(COLON);
  }

}
