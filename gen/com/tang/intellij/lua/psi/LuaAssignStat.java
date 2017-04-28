// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.comment.psi.api.LuaComment;

public interface LuaAssignStat extends LuaStatement, LuaDeclaration {

  @Nullable
  LuaExprList getExprList();

  @NotNull
  LuaVarList getVarList();

  LuaComment getComment();

  @NotNull
  PsiElement getAssign();

}
