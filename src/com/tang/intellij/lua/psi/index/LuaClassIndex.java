package com.tang.intellij.lua.psi.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/11/28.
 */
public class LuaClassIndex extends StringStubIndexExtension<LuaDocClassDef> {

    public static final StubIndexKey<String, LuaDocClassDef> KEY = StubIndexKey.createIndexKey("lua.index.class");

    private static final LuaClassIndex INSTANCE = new LuaClassIndex();

    public static LuaClassIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, LuaDocClassDef> getKey() {
        return KEY;
    }
}
