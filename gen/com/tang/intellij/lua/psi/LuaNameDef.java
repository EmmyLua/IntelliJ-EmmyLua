// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.SearchScope;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

public interface LuaNameDef extends LuaName, LuaTypeGuessable, PsiNameIdentifierOwner {

  LuaTypeSet guessType(SearchContext context);

  PsiElement getNameIdentifier();

  SearchScope getUseScope();

}
