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

package com.tang.intellij.lua.refactoring;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.psi.LuaVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RefactoringUtil
 * Created by tangzx on 2017/4/30.
 */
public class LuaRefactoringUtil {
    @NotNull
    public static List<PsiElement> getOccurrences(@NotNull final PsiElement pattern, @Nullable final PsiElement context) {
        if (context == null) {
            return Collections.emptyList();
        }
        final List<PsiElement> occurrences = new ArrayList<>();
        final LuaVisitor visitor = new LuaVisitor() {
            public void visitElement(@NotNull final PsiElement element) {
                if (PsiEquivalenceUtil.areElementsEquivalent(element, pattern)) {
                    occurrences.add(element);
                    return;
                }
                element.acceptChildren(this);
            }
        };
        context.acceptChildren(visitor);
        return occurrences;
    }

    public static boolean isLuaIdentifier(String name) {
        return StringUtil.isJavaIdentifier(name);
    }
}
