// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaParamInfo;
import com.tang.intellij.lua.ty.ITy;

public interface LuaDocFunctionTy extends LuaDocTy {

  @Nullable
  LuaDocFunctionParams getFunctionParams();

  @Nullable
  LuaDocFunctionReturnList getFunctionReturnList();

  @NotNull
  List<LuaDocGenericDef> getGenericDefList();

  @NotNull
  ITy getType();

  @Nullable
  LuaParamInfo[] getParams();

  @Nullable
  ITy getVarargParam();

  @Nullable
  ITy getReturnType();

}
