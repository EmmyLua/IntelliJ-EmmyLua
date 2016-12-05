package com.tang.intellij.lua.psi.stub;

import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.psi.LuaClassMethodFuncDef;

/**
 * class method static/instance
 * Created by tangzx on 2016/12/4.
 */
public interface LuaClassMethodStub extends StubElement<LuaClassMethodFuncDef> {

    String getClassName();
}
