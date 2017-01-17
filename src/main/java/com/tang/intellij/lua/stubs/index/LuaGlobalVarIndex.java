package com.tang.intellij.lua.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.psi.LuaGlobalVar;
import com.tang.intellij.lua.search.SearchContext;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

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

    public static LuaGlobalVar find(String key, SearchContext context) {
        if (context.isDumb())
            return null;

        Collection<LuaGlobalVar> vars = new SmartList<>();
        StubIndex.getInstance().processElements(KEY, key, context.getProject(), context.getScope(), LuaGlobalVar.class, (s) -> {
            vars.add(s);
            return true;
        });
        if (!vars.isEmpty()) {
            return vars.iterator().next();
        }
        return null;
    }
}
