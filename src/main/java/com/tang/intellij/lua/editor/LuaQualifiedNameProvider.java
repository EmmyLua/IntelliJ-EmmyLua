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

package com.tang.intellij.lua.editor;

import com.intellij.ide.actions.QualifiedNameProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.LogicalRoot;
import com.intellij.util.LogicalRootsManager;
import com.intellij.util.ObjectUtils;
import com.tang.intellij.lua.psi.LuaFile;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class LuaQualifiedNameProvider implements QualifiedNameProvider {
    @Nullable
    @Override
    public PsiElement adjustElementToCopy(PsiElement psiElement) {
        return null;
    }

    @Nullable
    @Override
    public String getQualifiedName(PsiElement psiElement) {
        if (psiElement instanceof LuaFile) {
            LuaFile file = (LuaFile) psiElement;
            VirtualFile virtualFile = file.getVirtualFile();
            Project project = file.getProject();

            final LogicalRoot logicalRoot = LogicalRootsManager.getLogicalRootsManager(project).findLogicalRoot(virtualFile);
            VirtualFile logicalRootFile = logicalRoot != null ? logicalRoot.getVirtualFile() : null;
            if (logicalRootFile != null && !virtualFile.equals(logicalRootFile)) {
                String value = ObjectUtils.assertNotNull(VfsUtilCore.getRelativePath(virtualFile, logicalRootFile, '.'));
                int lastDot = value.lastIndexOf('.');
                if (lastDot != -1)
                    value = value.substring(0, lastDot);
                return value;
            }
        }
        return null;
    }

    @Override
    public PsiElement qualifiedNameToElement(String s, Project project) {
        return null;
    }

    @Override
    public void insertQualifiedName(String s, PsiElement psiElement, Editor editor, Project project) {

    }
}
