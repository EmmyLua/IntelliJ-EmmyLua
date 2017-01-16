package com.tang.intellij.lua.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.psi.LuaGlobalVar;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/1/16.
 */
public class LuaGlobalVarIndex extends StringStubIndexExtension<LuaGlobalVar> {

    public static final StubIndexKey<String, LuaGlobalVar> KEY = StubIndexKey.createIndexKey("lua.index.global.var");

    private static final LuaGlobalVarIndex INSTANCE = new LuaGlobalVarIndex();

    public static LuaGlobalVarIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, LuaGlobalVar> getKey() {
        return KEY;
    }
}
