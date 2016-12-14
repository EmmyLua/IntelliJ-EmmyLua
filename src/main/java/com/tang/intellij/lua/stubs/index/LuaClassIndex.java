package com.tang.intellij.lua.stubs.index;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.comment.psi.LuaDocClassDef;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

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

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    @NotNull
    @Override
    public StubIndexKey<String, LuaDocClassDef> getKey() {
        return KEY;
    }

    public static LuaDocClassDef find(String name, Project project, GlobalSearchScope searchScope) {
        Collection<LuaDocClassDef> list = getInstance().get(name, project, searchScope);
        if (!list.isEmpty()) return list.iterator().next();
        return null;
    }
}
