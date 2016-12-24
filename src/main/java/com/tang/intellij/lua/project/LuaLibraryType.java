package com.tang.intellij.lua.project;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.tang.intellij.lua.lang.LuaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class LuaLibraryType extends LibraryType<DummyLibraryProperties> {

    public static LuaLibraryType instance() {
        return (LuaLibraryType) LibraryType.findByKind(LuaLibraryKind.INSTANCE);
    }

    protected LuaLibraryType() {
        super(LuaLibraryKind.INSTANCE);
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
        LibraryRootsComponentDescriptor chooser = createLibraryRootsComponentDescriptor();
        final VirtualFile[] files = FileChooser.chooseFiles(chooser.createAttachFilesChooserDescriptor("lua"), project, contextDirectory);

        return new NewLibraryConfiguration(suggestLibraryName(files),this, new DummyLibraryProperties()){

            @Override
            public void addRoots(@NotNull LibraryEditor libraryEditor) {
                for (VirtualFile file : files) {
                    libraryEditor.addRoot(file, OrderRootType.CLASSES);
                }
            }
        };
    }

    private static String suggestLibraryName(@NotNull VirtualFile[] classesRoots) {
        return classesRoots.length >= 1? FileUtil.getNameWithoutExtension(PathUtil.getFileName(classesRoots[0].getPath())):"Unnamed";
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
