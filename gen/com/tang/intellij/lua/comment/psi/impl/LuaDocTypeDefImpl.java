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
import com.tang.intellij.lua.stubs.LuaDocTyStub;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.Ty;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class LuaDocTypeDefImpl extends StubBasedPsiElementBase<LuaDocTyStub> implements LuaDocTypeDef {

  public LuaDocTypeDefImpl(LuaDocTyStub stub, IStubElementType type) {
    super(stub, type);
  }

  public LuaDocTypeDefImpl(ASTNode node) {
    super(node);
  }

  public LuaDocTypeDefImpl(LuaDocTyStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaDocVisitor visitor) {
    visitor.visitTypeDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaDocVisitor) accept((LuaDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public LuaDocCommentString getCommentString() {
    return PsiTreeUtil.getChildOfType(this, LuaDocCommentString.class);
  }

  @Override
  @Nullable
  public LuaDocTypeSet getTypeSet() {
    return PsiTreeUtil.getChildOfType(this, LuaDocTypeSet.class);
  }

  @NotNull
  public Ty guessType(SearchContext context) {
    return LuaDocPsiImplUtilKt.guessType(this, context);
  }

}
