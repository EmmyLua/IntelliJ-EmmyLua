// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface LuaBlock extends LuaPsiElement {

  @NotNull
  List<LuaAssignStat> getAssignStatList();

  @NotNull
  List<LuaClassMethodDef> getClassMethodDefList();

  @NotNull
  List<LuaFuncDef> getFuncDefList();

  @NotNull
  List<LuaLocalDef> getLocalDefList();

  @NotNull
  List<LuaLocalFuncDef> getLocalFuncDefList();

  @NotNull
  List<LuaStatement> getStatementList();

}
