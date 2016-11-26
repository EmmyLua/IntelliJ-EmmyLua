package com.tang.intellij.lua.psi.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class GlobalFuncIndex extends StringStubIndexExtension<LuaGlobalFuncDef> {

    public static final StubIndexKey<String, LuaGlobalFuncDef> KEY = StubIndexKey.createIndexKey("lua.index.type.name");

    private static final GlobalFuncIndex INSTANCE = new GlobalFuncIndex();

    public static GlobalFuncIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, LuaGlobalFuncDef> getKey() {
        return KEY;
    }
}
