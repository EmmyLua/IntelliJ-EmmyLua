// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaTableStub;

public interface LuaTableExpr extends LuaExpr, LuaIndentRange, StubBasedPsiElement<LuaTableStub> {

  @Nullable
  LuaFieldList getFieldList();

  @Nullable
  LuaTableField findField(String fieldName);

}
