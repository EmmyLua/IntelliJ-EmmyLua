package com.tang.intellij.lua.search;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;

/**
 *
 * Created by tangzx on 2017/1/14.
 */
public class SearchContext {

    public Project getProject() {
        return project;
    }

    private Project project;

    public SearchContext setCurrentStubFile(PsiFile currentStubFile) {
        this.currentStubFile = currentStubFile;
        return this;
    }

    private PsiFile currentStubFile;

    public SearchContext(Project project) {
        this.project = project;
    }

    private GlobalSearchScope scope;

    public GlobalSearchScope getScope() {
        if (scope == null) {
            scope = new ProjectAndLibrariesScope(project);
            if (currentStubFile != null) {
                VirtualFile virtualFile = currentStubFile.getViewProvider().getVirtualFile();
                GlobalSearchScope not = GlobalSearchScope.notScope(GlobalSearchScope.fileScope(project, virtualFile));
                scope = scope.intersectWith(not);
                scope = GlobalSearchScope.EMPTY_SCOPE;
            }
        }
        return scope;
    }

}
