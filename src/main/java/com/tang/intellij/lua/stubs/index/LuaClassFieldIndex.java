package com.tang.intellij.lua.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaClassFieldIndex extends StringStubIndexExtension<LuaClassField> {

    public static final StubIndexKey<String, LuaClassField> KEY = StubIndexKey.createIndexKey("lua.index.class.field");

    private static final LuaClassFieldIndex INSTANCE = new LuaClassFieldIndex();

    public static LuaClassFieldIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    @NotNull
    @Override
    public StubIndexKey<String, LuaClassField> getKey() {
        return KEY;
    }

    public static LuaClassField find(@NotNull String key, @NotNull SearchContext context) {
        if (context.isDumb())
            return null;

        Collection<LuaClassField> list = INSTANCE.get(key, context.getProject(), context.getScope());
        if (!list.isEmpty())
            return list.iterator().next();
        return null;
    }

    public static LuaClassField find(@NotNull String className, @NotNull String fieldName, @NotNull SearchContext context) {
        return find(className + "." + fieldName, context);
    }
}
