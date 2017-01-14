package com.tang.intellij.lua.stubs;

import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.psi.LuaTableField;

/**
 * table field stub
 * Created by tangzx on 2017/1/14.
 */
public interface LuaTableFieldStub extends StubElement<LuaTableField> {
    String getTypeName();
    String getFieldName();
}
