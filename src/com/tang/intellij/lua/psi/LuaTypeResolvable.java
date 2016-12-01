package com.tang.intellij.lua.psi;

import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;

/**
 *
 * Created by tangzx on 2016/12/1.
 */
public interface LuaTypeResolvable extends PsiElement{
    LuaDocClassDef resolveType();
}
