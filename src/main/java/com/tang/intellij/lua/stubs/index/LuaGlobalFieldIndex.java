package com.tang.intellij.lua.stubs.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.comment.psi.LuaDocGlobalDef;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.search.SearchContext;
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

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    public static LuaDocGlobalDef find(String key, SearchContext context) {
        try {
            Collection<LuaDocGlobalDef> defs = LuaGlobalFieldIndex.getInstance().get(key, context.getProject(), context.getScope());
            if (!defs.isEmpty()) {
                return defs.iterator().next();
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }
}
