// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.tang.intellij.lua.psi.LuaClass;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocTagClassStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.ty.ITyClass;

public interface LuaDocTagClass extends LuaDocPsiElement, PsiNameIdentifierOwner, LuaClass, LuaDocTag, StubBasedPsiElement<LuaDocTagClassStub> {

  @Nullable
  LuaDocCommentString getCommentString();

  @NotNull
  List<LuaDocGenericDef> getGenericDefList();

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
  LuaDocClassRef getSuperClassRef();

  @Nullable
  PsiElement getModule();

}
