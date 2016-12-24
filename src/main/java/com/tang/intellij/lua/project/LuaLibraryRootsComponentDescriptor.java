package com.tang.intellij.lua.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.AttachRootButtonDescriptor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.libraries.ui.OrderRootTypePresentation;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * Created by tangzx on 2016/12/24.
 */
public class LuaLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {
    @Nullable
    @Override
    public OrderRootTypePresentation getRootTypePresentation(@NotNull OrderRootType orderRootType) {
        return null;
    }

    @NotNull
    @Override
    public List<? extends RootDetector> getRootDetectors() {
        return Collections.singletonList(new LuaRootDetector(OrderRootType.SOURCES, false, "Lua Sources"));
    }

    @NotNull
    @Override
    public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
        return Collections.singletonList(new LuaAttachRootButtonDescriptor(OrderRootType.CLASSES, "XXX"));
    }

    static class LuaRootDetector extends RootDetector {

        LuaRootDetector(OrderRootType rootType, boolean jarDirectory, String presentableRootTypeName) {
            super(rootType, jarDirectory, presentableRootTypeName);
        }

        @NotNull
        @Override
        public Collection<VirtualFile> detectRoots(@NotNull VirtualFile virtualFile, @NotNull ProgressIndicator progressIndicator) {
            return Collections.singleton(virtualFile);
        }
    }

    static class LuaAttachRootButtonDescriptor extends AttachRootButtonDescriptor {

        LuaAttachRootButtonDescriptor(@NotNull OrderRootType orderRootType, @NotNull String buttonText) {
            super(orderRootType, buttonText);
        }

        @Override
        public VirtualFile[] selectFiles(@NotNull JComponent jComponent, @Nullable VirtualFile virtualFile, @Nullable Module module, @NotNull LibraryEditor libraryEditor) {
            return new VirtualFile[0];
        }
    }
}
