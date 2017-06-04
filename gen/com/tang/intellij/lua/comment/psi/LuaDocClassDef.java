// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocClassStub;
import com.intellij.navigation.ItemPresentation;
import com.tang.intellij.lua.lang.type.LuaType;

public interface LuaDocClassDef extends LuaDocPsiElement, PsiNameIdentifierOwner, StubBasedPsiElement<LuaDocClassStub> {

  @Nullable
  LuaDocCommentString getCommentString();

  @NotNull
  PsiElement getId();

  LuaType getClassType();

  ItemPresentation getPresentation();

  PsiElement getNameIdentifier();

  PsiElement setName(String newName);

  String getName();

  int getTextOffset();

  @Nullable
  LuaDocClassNameRef getSuperClassNameRef();

}
