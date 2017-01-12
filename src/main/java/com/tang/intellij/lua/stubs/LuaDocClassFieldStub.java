package com.tang.intellij.lua.stubs;

import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public interface LuaDocClassFieldStub extends StubElement<LuaDocFieldDef> {
    String getName();

    StringRef getClassName();
}
