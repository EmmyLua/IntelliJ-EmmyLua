package com.tang.intellij.lua.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class LuaGlobalFuncIndex extends StringStubIndexExtension<LuaGlobalFuncDef> {

    public static final StubIndexKey<String, LuaGlobalFuncDef> KEY = StubIndexKey.createIndexKey("lua.index.global_function");

    private static final LuaGlobalFuncIndex INSTANCE = new LuaGlobalFuncIndex();

    public static LuaGlobalFuncIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    @NotNull
    @Override
    public StubIndexKey<String, LuaGlobalFuncDef> getKey() {
        return KEY;
    }

    public static LuaGlobalFuncDef find(String key, SearchContext context) {
        try {
            Collection<LuaGlobalFuncDef> defs = LuaGlobalFuncIndex.getInstance().get(key, context.getProject(), context.getScope());
            if (!defs.isEmpty()) {
                return defs.iterator().next();
            }
        } catch (Exception ignored) {

        }
        return null;
    }
}
