package com.tang.intellij.lua.psi.index;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
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

    @NotNull
    @Override
    public StubIndexKey<String, LuaGlobalFuncDef> getKey() {
        return KEY;
    }

    public static LuaGlobalFuncDef find(String key, Project project, GlobalSearchScope scope) {
        try {

            Collection<LuaGlobalFuncDef> defs = LuaGlobalFuncIndex.getInstance().get(key, project, new ProjectAndLibrariesScope(project));
            if (!defs.isEmpty()) {
                return defs.iterator().next();
            }
        } catch (Exception ignored) {

        }
        return null;
    }
}
