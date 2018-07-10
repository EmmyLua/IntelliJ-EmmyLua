// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.tang.intellij.lua.stubs.LuaExprStubElement;
import com.tang.intellij.lua.stubs.LuaNameExprStub;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;

public interface LuaNameExpr extends LuaExpr, PsiNameIdentifierOwner, LuaExprStubElement<LuaNameExprStub>, LuaModuleClassField {

  @NotNull
  PsiElement getId();

  @NotNull
  PsiElement setName(String name);

  @NotNull
  String getName();

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  PsiReference[] getReferences();

  boolean isDeprecated();

  //WARNING: toString(...) is skipped
  //matching toString(LuaNameExpr, ...)
  //methods are not found in LuaPsiImplUtilKt

}
