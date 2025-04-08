// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;

import com.tang.intellij.lua.ty.TyAliasSubstitutor;
import groovy.lang.Tuple4;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.ty.ITy;
import com.tang.intellij.lua.stubs.LuaExprPlaceStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.stubs.LuaExprStub;

public class LuaCallExprImpl extends LuaCallExprMixin implements LuaCallExpr {

  public LuaCallExprImpl(@NotNull LuaExprPlaceStub stub, @NotNull IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public LuaCallExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LuaCallExprImpl(@NotNull LuaExprPlaceStub stub, @NotNull IElementType type, @NotNull ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitCallExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public LuaArgs getArgs() {
    return notNullChild(PsiTreeUtil.getStubChildOfType(this, LuaArgs.class));
  }

  @Override
  @NotNull
  public LuaExpr getExpr() {
    return notNullChild(PsiTreeUtil.getStubChildOfType(this, LuaExpr.class));
  }

  @Override
  @NotNull
  public ITy guessParentType(@NotNull SearchContext context) {
    return LuaPsiImplUtilKt.guessParentType(this, context);
  }

  @Override
  @NotNull
  public ITy guessType(SearchContext context) {
    ITy ty = SearchContext.Companion.infer(this, context);
    String typeName = ty.getDisplayName();
    boolean isDynamic = false;
    Tuple4<Boolean, String, String, String> t;
    do
    {
      t = getStringArgByTypeName(typeName, "UseArgString");
      isDynamic = t.getV1();
      if(isDynamic) break;

      t = getStringArgByTypeName(typeName, "UseArgName");
      isDynamic = t.getV1();
      if (isDynamic) break;

      t = getStringArgByTypeName(typeName, "UseArgFullName");
      isDynamic = t.getV1();
      if (isDynamic) break;

    }while (false);

    if(isDynamic) {
      ty = LuaPsiImplUtilKt.newType((String)t.getV2(), ty, (String)t.getV3(), (String)t.getV4());
    }

    ty = TyAliasSubstitutor.Companion.substitute(ty, context);
    return ty;
  }

  @Nullable
  public Tuple4<Boolean, String, String, String> getStringArgByTypeName(String typeName, String argType) {
    int start = 0;
    int index = 0;
    String replaceStr = "";
    String str = "";
    Boolean result = false;

    if ((start = typeName.indexOf(argType)) >= 0) {
      int length = argType.length();
      replaceStr = argType;
      if(start + length < typeName.length())
      {
        index = typeName.charAt(start + length) - '1';
        replaceStr = argType + Integer.toString(index + 1);
      }

      if(index >= 0 && index <= 9)
      {
        result = true;
        if (argType == "UseArgString")
        {
          PsiElement p = LuaPsiImplUtilKt.getStringArgByIndex(this, index);
          str = LuaPsiImplUtilKt.getStringValue(p);
        }
        else if (argType == "UseArgName")
        {
          PsiElement p = LuaPsiImplUtilKt.getParamNameByIndex(this, index);
          str = LuaPsiImplUtilKt.getParamStringValue(p);
        }
        else if (argType == "UseArgFullName")
        {
          PsiElement p = LuaPsiImplUtilKt.getParamNameByIndex(this, index);
          str = LuaPsiImplUtilKt.getParamAllStringValue(p);
        }
        if (str != "") {
          typeName = typeName.replace(replaceStr, str);
        }
      }
    }
    return new Tuple4<Boolean, String, String, String>(result, typeName, replaceStr, str);
  }


  @Nullable
  public PsiElement getFirstStringArg() {
    return LuaPsiImplUtilKt.getStringArgByIndex(this, 0);
  }

  @Nullable
  public PsiElement getFirstParamArg() {
    return LuaPsiImplUtilKt.getParamNameByIndex(this, 0);
  }

  @Override
  public boolean isMethodDotCall() {
    return LuaPsiImplUtilKt.isMethodDotCall(this);
  }

  @Override
  public boolean isMethodColonCall() {
    return LuaPsiImplUtilKt.isMethodColonCall(this);
  }

  @Override
  public boolean isFunctionCall() {
    return LuaPsiImplUtilKt.isFunctionCall(this);
  }

}
