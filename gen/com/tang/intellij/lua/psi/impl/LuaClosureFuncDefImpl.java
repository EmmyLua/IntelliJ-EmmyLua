// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.tang.intellij.lua.psi.LuaTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public class LuaClosureFuncDefImpl extends ASTWrapperPsiElement implements LuaClosureFuncDef {

  public LuaClosureFuncDefImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull LuaVisitor visitor) {
    visitor.visitClosureFuncDef(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LuaVisitor) accept((LuaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public LuaFuncBody getFuncBody() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LuaFuncBody.class));
  }

  public List<LuaParamNameDef> getParamNameDefList() {
    return LuaPsiImplUtil.getParamNameDefList(this);
  }

  public LuaTypeSet guessReturnTypeSet(SearchContext searchContext) {
    return LuaPsiImplUtil.guessReturnTypeSet(this, searchContext);
  }

  @NotNull
  public LuaParamInfo[] getParams() {
    return LuaPsiImplUtil.getParams(this);
  }

}
