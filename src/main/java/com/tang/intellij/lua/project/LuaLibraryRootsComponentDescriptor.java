/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.project;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.AttachRootButtonDescriptor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.libraries.ui.OrderRootTypePresentation;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    public FileChooserDescriptor createAttachFilesChooserDescriptor(@Nullable String libraryName) {
        return new FileChooserDescriptor(super.createAttachFilesChooserDescriptor(libraryName)) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                if (file.isDirectory())
                    return true;

                String ext = file.getExtension();
                return ext != null && ext.equalsIgnoreCase("zip");
            }
        };
    }

    @NotNull
    @Override
    public List<? extends RootDetector> getRootDetectors() {
        return Collections.singletonList(new LuaRootDetector(OrderRootType.CLASSES, true, "Lua Sources"));
    }

    @NotNull
    @Override
    public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
        return new ArrayList<>();
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
}
