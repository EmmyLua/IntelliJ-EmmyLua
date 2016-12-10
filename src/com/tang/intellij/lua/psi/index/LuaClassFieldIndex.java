package com.tang.intellij.lua.psi.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.doc.psi.LuaDocFieldDef;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaClassFieldIndex extends StringStubIndexExtension<LuaDocFieldDef> {

    public static final StubIndexKey<String, LuaDocFieldDef> KEY = StubIndexKey.createIndexKey("lua.index.class.field");

    private static final LuaClassFieldIndex INSTANCE = new LuaClassFieldIndex();

    public static LuaClassFieldIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, LuaDocFieldDef> getKey() {
        return KEY;
    }
}
