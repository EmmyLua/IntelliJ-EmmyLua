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
