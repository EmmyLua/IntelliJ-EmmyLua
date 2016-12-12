package com.tang.intellij.lua.psi.stub;

import com.intellij.psi.stubs.StubElement;
import com.tang.intellij.lua.comment.psi.LuaDocGlobalDef;

/**
 *
 * Created by tangzx on 2016/12/9.
 */
public interface LuaGlobalFieldStub extends StubElement<LuaDocGlobalDef> {
    String[] getNames();
}
