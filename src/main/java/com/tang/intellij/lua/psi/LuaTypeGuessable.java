package com.tang.intellij.lua.psi;

import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.search.SearchContext;

/**
 *
 * Created by tangzx on 2016/12/1.
 */
public interface LuaTypeGuessable extends LuaPsiElement {
    LuaTypeSet guessType(SearchContext context);
}
