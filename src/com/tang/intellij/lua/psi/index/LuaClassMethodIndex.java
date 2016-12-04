package com.tang.intellij.lua.psi.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.psi.LuaClassMethodFuncDef;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodIndex extends StringStubIndexExtension<LuaClassMethodFuncDef> {
    public static final StubIndexKey<String, LuaClassMethodFuncDef> KEY = StubIndexKey.createIndexKey("lua.index.class.method");

    private static final LuaClassMethodIndex INSTANCE = new LuaClassMethodIndex();

    public static LuaClassMethodIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, LuaClassMethodFuncDef> getKey() {
        return KEY;
    }
}
