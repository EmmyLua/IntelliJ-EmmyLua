package com.tang.intellij.lua.psi.index;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *
 * Created by tangzx on 2016/12/4.
 */
public class LuaClassMethodIndex extends StringStubIndexExtension<LuaClassMethodDef> {
    public static final StubIndexKey<String, LuaClassMethodDef> KEY = StubIndexKey.createIndexKey("lua.index.class.method");

    private static final LuaClassMethodIndex INSTANCE = new LuaClassMethodIndex();

    public static LuaClassMethodIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, LuaClassMethodDef> getKey() {
        return KEY;
    }

    public static Collection<LuaClassMethodDef> findStaticMethods(String className, Project project, GlobalSearchScope scope) {
        String key = className + ".static";
        return INSTANCE.get(key, project, scope);
    }
}
