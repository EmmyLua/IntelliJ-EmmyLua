// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocClassStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.ty.ITyClass;

public interface LuaDocTagClass extends LuaDocPsiElement, PsiNameIdentifierOwner, LuaDocTag, StubBasedPsiElement<LuaDocClassStub> {

  @Nullable
  LuaDocCommentString getCommentString();

  @NotNull
  PsiElement getId();

  @NotNull
  ITyClass getType();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  PsiElement setName(@NotNull String newName);

  @NotNull
  String getName();

  int getTextOffset();

  boolean isDeprecated();

  @Nullable
  LuaDocClassNameRef getSuperClassNameRef();

  @Nullable
  PsiElement getModule();

}
