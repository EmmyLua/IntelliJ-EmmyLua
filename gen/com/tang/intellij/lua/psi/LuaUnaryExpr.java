// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.stubs.LuaExprStubElement;
import com.tang.intellij.lua.stubs.LuaUnaryExprStub;

public interface LuaUnaryExpr extends LuaExpr, LuaExprStubElement<LuaUnaryExprStub> {

  @Nullable
  LuaExpr getExpr();

  @NotNull
  LuaUnaryOp getUnaryOp();

}
