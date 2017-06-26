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
import com.tang.intellij.lua.lang.GuessTypeKind;

/**
 *
 * Created by tangzx on 2017/1/14.
 */
public class SearchContext {

    private PsiFile currentStubFile;
    private GlobalSearchScope scope;
    private Project project;
    private int guessTypeKind = GuessTypeKind.Standard;

    public SearchContext(Project project) {
        this.project = project;
    }

    public void setGuessTypeKind(int value) {
        guessTypeKind = value;
    }

    public boolean isGuessTypeKind(int kind) {
        return (guessTypeKind & kind) == kind;
    }

    public Project getProject() {
        return project;
    }

    public SearchContext setCurrentStubFile(PsiFile currentStubFile) {
        this.currentStubFile = currentStubFile;
        return this;
    }

    public GlobalSearchScope getScope() {
        if (scope == null) {
            if (isDumb()) {
                scope = GlobalSearchScope.EMPTY_SCOPE;
            } else {
                scope = new ProjectAndLibrariesScope(project);
            }
        }
        return scope;
    }

    public boolean isDumb() {
        return DumbService.isDumb(project) || currentStubFile != null;
    }
}
