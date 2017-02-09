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
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2017/1/14.
 */
public class SearchContext {

    private PsiFile currentStubFile;
    private GlobalSearchScope scope;
    private Project project;
    private SearchContext parent;
    private int type = -1;
    private Object obj;

    public SearchContext(Project project) {
        this.project = project;
    }

    public SearchContext(SearchContext parent, int type, @NotNull Object o) {
        this(parent.getProject());
        this.parent = parent;
        assert type >= 0;
        this.type = type;
        this.obj = o;
        setCurrentStubFile(parent.currentStubFile);
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
            if (parent != null) {
                scope = parent.getScope();
            } else {
                scope = new ProjectAndLibrariesScope(project);
                if (isDumb()) {
                    scope = GlobalSearchScope.EMPTY_SCOPE;
                }
            }
        }
        return scope;
    }

    public boolean isDeadLock(int times) {
        if (type == -1) return false;
        int matchTimes = 0;
        SearchContext cur = parent;
        while (cur != null) {
            if (cur.type == type && cur.obj == obj) {
                matchTimes++;
            }
            cur = cur.parent;
        }
        return matchTimes >= times;
    }

    public boolean isDumb() {
        return DumbService.isDumb(project) || currentStubFile != null;
    }

    public static int TYPE_FILE_RETURN = 0;
    public static int TYPE_BODY_OWNER = 1;
    public static int TYPE_VALUE_EXPR = 2;

    public static SearchContext wrapDeadLock(SearchContext parent, int type, @NotNull Object o) {
        return new SearchContext(parent, type, o);
    }
}
