// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;

import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.comment.psi.LuaDocPsiImplUtilKt;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.LuaExprStubElement;
import com.tang.intellij.lua.ty.ITy;
import com.tang.intellij.lua.ty.TyAliasSubstitutor;
import kotlin.reflect.jvm.internal.impl.resolve.constants.StringValue;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.stubs.LuaLiteralExprStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.stubs.LuaExprStub;

public class LuaLiteralExprImpl extends LuaLiteralExprMixin implements LuaLiteralExpr, LuaModuleClassField, LuaExprStubElement<LuaLiteralExprStub> {

  public LuaLiteralExprImpl(@NotNull LuaLiteralExprStub stub, @NotNull IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public LuaLiteralExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaLiteralExprImpl(@NotNull LuaLiteralExprStub stub, @NotNull IElementType type, @NotNull ASTNode node) {
    super(stub, type, node);
  }

  @Nullable
  public PsiElement getId() {
    return this;
  }
  @Nullable
  public PsiElement getNameIdentifier() {
    return LuaPsiImplUtilKt.getNameIdentifier(this);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitLiteralExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @NotNull
  @Override
  public Visibility getVisibility() {
    return LuaDocPsiImplUtilKt.getVisibility(this);
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  public String getName() {
    return LuaPsiImplUtilKt.getName(this);
  }

  @NotNull
  public PsiElement setName(@NotNull String name) {
    LuaPsiImplUtilKt.setName(this, name);
    return this;
  }

  @NotNull
  @Override
  public ITy guessType(@NotNull SearchContext context) {
    String moduleName = LuaPsiImplUtilKt.getModuleName(this);
    String name = LuaPsiImplUtilKt.getName(this);
    if (moduleName != null && moduleName == name) {
      ITy ty = SearchContext.Companion.NewTyClass(moduleName);
      ty = TyAliasSubstitutor.Companion.substitute(ty, context);

      return ty;
    }
    else {
      ITy ty = SearchContext.Companion.infer(this, context);
      ty = TyAliasSubstitutor.Companion.substitute(ty, context);

      return ty;
    }
  }

  @NotNull
  @Override
  public ITy guessParentType(@NotNull SearchContext context) {

    String name = LuaPsiImplUtilKt.getName(this);
    ITy ty = SearchContext.Companion.NewTyClass((name));
    ty = TyAliasSubstitutor.Companion.substitute(ty, context);
    return ty;
  }
}
