package com.tang.intellij.lua.psi;

import com.intellij.navigation.NavigationItem;
import com.tang.intellij.lua.lang.type.LuaTypeSet;

/**
 * 类的属性字段
 * Created by tangzx on 2016/12/21.
 */
public interface LuaClassField extends LuaClassMember, NavigationItem {
    LuaTypeSet resolveType();
    String getFieldName();
}
