// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.stubs.LuaExprStubElement;
import com.tang.intellij.lua.stubs.LuaBinaryExprStub;

public interface LuaBinaryExpr extends LuaExpr, LuaExprStubElement<LuaBinaryExprStub> {

  @NotNull
  LuaBinaryOp getBinaryOp();

  @Nullable
  LuaExpr getExpr();

}
