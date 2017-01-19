package com.tang.intellij.lua.stubs.index;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * Created by TangZX on 2017/1/19.
 */
public class LuaShortNameIndex extends StringStubIndexExtension<NavigatablePsiElement> {

    public static final StubIndexKey<String, NavigatablePsiElement> KEY = StubIndexKey.createIndexKey("lua.index.short_name");

    private static final LuaShortNameIndex INSTANCE = new LuaShortNameIndex();

    public static LuaShortNameIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() { return LuaLanguage.INDEX_VERSION;}

    @NotNull
    @Override
    public StubIndexKey<String, NavigatablePsiElement> getKey() {
        return KEY;
    }

    @NotNull
    public static Collection<NavigatablePsiElement> find(String key, SearchContext searchContext) {
        if (searchContext.isDumb())
            return Collections.emptyList();
        return INSTANCE.get(key, searchContext.getProject(), searchContext.getScope());
    }
}
