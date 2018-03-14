// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaPlaceholderStub;

public interface LuaClassMethodName extends LuaPsiElement, StubBasedPsiElement<LuaPlaceholderStub> {

  @NotNull
  LuaExpr getExpr();

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getDot();

  @Nullable
  PsiElement getColon();

}
