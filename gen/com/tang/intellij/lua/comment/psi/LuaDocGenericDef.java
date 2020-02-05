// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.tang.intellij.lua.psi.LuaClass;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocGenericDefStub;

public interface LuaDocGenericDef extends PsiNameIdentifierOwner, LuaDocPsiElement, LuaClass, StubBasedPsiElement<LuaDocGenericDefStub> {

  @Nullable
  LuaDocClassRef getClassRef();

  @NotNull
  PsiElement getId();

  @Nullable
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @Nullable
  String getName();

  int getTextOffset();

}
