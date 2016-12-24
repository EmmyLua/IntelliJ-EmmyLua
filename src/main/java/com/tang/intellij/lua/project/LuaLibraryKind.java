package com.tang.intellij.lua.project;

import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class LuaLibraryKind extends PersistentLibraryKind<DummyLibraryProperties> {

    public static LuaLibraryKind INSTANCE = new LuaLibraryKind();

    private LuaLibraryKind() {
        super("Lua");
    }

    @NotNull
    @Override
    public DummyLibraryProperties createDefaultProperties() {
        return new DummyLibraryProperties();
    }
}
