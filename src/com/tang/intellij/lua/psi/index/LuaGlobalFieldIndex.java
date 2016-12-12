package com.tang.intellij.lua.psi.index;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.comment.psi.LuaDocGlobalDef;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 *
 * Created by tangzx on 2016/12/10.
 */
public class LuaGlobalFieldIndex extends StringStubIndexExtension<LuaDocGlobalDef> {
    @NotNull
    @Override
    public StubIndexKey<String, LuaDocGlobalDef> getKey() {
        return KEY;
    }

    public static final StubIndexKey<String, LuaDocGlobalDef> KEY = StubIndexKey.createIndexKey("lua.index.global.field");

    private static final LuaGlobalFieldIndex INSTANCE = new LuaGlobalFieldIndex();

    public static LuaGlobalFieldIndex getInstance() {
        return INSTANCE;
    }

    public static LuaDocGlobalDef find(String key, Project project, GlobalSearchScope scope) {
        try {
            Collection<LuaDocGlobalDef> defs = LuaGlobalFieldIndex.getInstance().get(key, project, scope);
            if (!defs.isEmpty()) {
                return defs.iterator().next();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
