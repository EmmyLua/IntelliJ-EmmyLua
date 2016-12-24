package com.tang.intellij.lua.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.*;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.tang.intellij.lua.lang.LuaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class LuaLibraryType extends LibraryType<DummyLibraryProperties> {
    protected LuaLibraryType() {
        super(new PersistentLibraryKind<DummyLibraryProperties>("Lua") {

            @NotNull
            @Override
            public DummyLibraryProperties createDefaultProperties() {
                return new DummyLibraryProperties();
            }
        });
    }

    @Nullable
    @Override
    public String getCreateActionName() {
        return "Lua Zip Library";
    }

    @Nullable
    @Override
    public Icon getIcon(@Nullable LibraryProperties properties) {
        return LuaIcons.FILE;
    }

    @Nullable
    @Override
    public NewLibraryConfiguration createNewLibrary(@NotNull JComponent parentComponent, @Nullable VirtualFile contextDirectory, @NotNull Project project) {
        return LibraryTypeService.getInstance()
                .createLibraryFromFiles(createLibraryRootsComponentDescriptor(), parentComponent, contextDirectory, this, project);
    }

    @NotNull
    @Override
    public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor() {
        return new LuaLibraryRootsComponentDescriptor();
    }

    @Nullable
    @Override
    public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent<DummyLibraryProperties> libraryEditorComponent) {
        return null;
    }
}
