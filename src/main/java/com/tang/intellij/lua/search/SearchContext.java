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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.tang.intellij.lua.lang.GuessTypeKind;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

/**
 *
 * Created by tangzx on 2017/1/14.
 */
public class SearchContext {

    private PsiFile currentStubFile;
    private GlobalSearchScope scope;
    private Project project;
    private SearchContext parent;
    private Object obj;
    private int guessTypeKind = GuessTypeKind.Standard;
    private Stack<Pair> deadLockStack = new Stack<>();

    class Pair {
        public Overflow type;
        public PsiElement object;
    }

    public SearchContext(Project project) {
        this.project = project;
    }

    public SearchContext(SearchContext parent, @NotNull Object o) {
        this(parent.getProject());
        this.parent = parent;
        this.obj = o;
        this.guessTypeKind = parent.guessTypeKind;
        setCurrentStubFile(parent.currentStubFile);
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

    public boolean push(PsiElement element, Overflow type) {
        boolean v = !checkDeadLock(element, type);
        if (v) {
            Pair pair = new Pair();
            pair.object = element;
            pair.type = type;
            deadLockStack.push(pair);
        }
        return v;
    }

    public void pop(PsiElement element) {
        Pair pair = deadLockStack.pop();
        assert pair.object == element;
    }

    private boolean checkDeadLock(PsiElement element, Overflow type) {
        for (Pair pair : deadLockStack) {
            if (type == pair.type && element == pair.object)
                return true;
        }
        return false;
    }

    public boolean isDumb() {
        return DumbService.isDumb(project) || currentStubFile != null;
    }

    public enum Overflow {
        Normal,
        GuessType,
        FileReturn,
        FindBodyOwner,
    }
}
