// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaTableFieldStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public interface LuaTableField extends LuaClassField, PsiNameIdentifierOwner, StubBasedPsiElement<LuaTableFieldStub> {

  @NotNull
  List<LuaExpr> getExprList();

  @Nullable
  PsiElement getId();

  PsiElement getNameIdentifier();

  PsiElement setName(String name);

  String getName();

  int getTextOffset();

  String getFieldName();

  ItemPresentation getPresentation();

  LuaTypeSet guessType(SearchContext context);

}
