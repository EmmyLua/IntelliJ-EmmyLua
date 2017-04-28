// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import  com.tang.intellij.lua.psi.LuaParametersOwner;
import  com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.comment.psi.api.LuaComment;

public interface LuaForBStat extends LuaStatement, LuaIndentRange, LuaParametersOwner, LuaCommentOwner {

  @Nullable
  LuaExprList getExprList();

  @NotNull
  List<LuaParamNameDef> getParamNameDefList();

  LuaComment getComment();

}
