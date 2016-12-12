package com.tang.intellij.lua.psi;

import com.tang.intellij.lua.lang.type.LuaTypeSet;

/**
 * 表达式，可以推算计算后的数据类型
 * Created by TangZX on 2016/12/2.
 */
public interface LuaExpression extends LuaPsiElement {

    // 表达式计算后的结果推算
    LuaTypeSet guessType();
}
