package com.tang.intellij.lua.psi;

import com.intellij.navigation.NavigationItem;

/**
 * 类的属性字段
 * Created by tangzx on 2016/12/21.
 */
public interface LuaClassField extends LuaClassMember, NavigationItem, LuaTypeGuessable {
    String getFieldName();
}
