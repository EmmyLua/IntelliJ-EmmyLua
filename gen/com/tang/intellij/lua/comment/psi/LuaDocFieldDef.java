// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaClassField;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocClassFieldStub;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public interface LuaDocFieldDef extends LuaClassField, LuaDocPsiElement, PsiNameIdentifierOwner, StubBasedPsiElement<LuaDocClassFieldStub> {

  @Nullable
  LuaDocAccessModifier getAccessModifier();

  @Nullable
  LuaDocCommentString getCommentString();

  @Nullable
  LuaDocTypeSet getTypeSet();

  @Nullable
  PsiElement getId();

  LuaTypeSet guessType(SearchContext context);

  PsiElement getNameIdentifier();

  PsiElement setName(String newName);

  String getName();

  int getTextOffset();

  String getFieldName();

}
