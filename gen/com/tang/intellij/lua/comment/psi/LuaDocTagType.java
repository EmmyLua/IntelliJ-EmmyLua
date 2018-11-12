// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.tang.intellij.lua.stubs.LuaDocTypeDefStub;
import com.tang.intellij.lua.ty.ITy;

public interface LuaDocTagType extends LuaDocTag, StubBasedPsiElement<LuaDocTypeDefStub> {

  @Nullable
  LuaDocCommentString getCommentString();

  @Nullable
  LuaDocTy getTy();

  @NotNull
  ITy getType();

}
