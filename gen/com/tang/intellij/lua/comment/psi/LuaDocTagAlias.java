// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaTypeAlias;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocTagAliasStub;
import com.tang.intellij.lua.ty.ITy;

public interface LuaDocTagAlias extends LuaTypeAlias, LuaDocPsiElement, PsiNameIdentifierOwner, LuaDocTag, StubBasedPsiElement<LuaDocTagAliasStub> {

  @NotNull
  List<LuaDocGenericDef> getGenericDefList();

  @Nullable
  LuaDocTy getTy();

  @NotNull
  PsiElement getId();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @NotNull
  String getName();

  int getTextOffset();

  @NotNull
  ITy getType();

}
