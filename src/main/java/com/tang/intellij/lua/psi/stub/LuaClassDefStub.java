package com.tang.intellij.lua.psi.stub;

import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;

/**
 *
 * Created by tangzx on 2016/11/28.
 */
public interface LuaClassDefStub extends StubElement<LuaDocClassDef> {
    String getClassName();
}
