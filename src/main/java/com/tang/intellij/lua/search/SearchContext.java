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

package com.tang.intellij.lua.search;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
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
            if (isDumb()) {
                /*VirtualFile virtualFile = currentStubFile.getViewProvider().getVirtualFile();
                GlobalSearchScope not = GlobalSearchScope.notScope(GlobalSearchScope.fileScope(project, virtualFile));
                scope = scope.intersectWith(not);*/
                scope = GlobalSearchScope.EMPTY_SCOPE;
            }
        }
        return scope;
    }

    public boolean isDumb() {
        return DumbService.isDumb(project) || currentStubFile != null;
    }
}
