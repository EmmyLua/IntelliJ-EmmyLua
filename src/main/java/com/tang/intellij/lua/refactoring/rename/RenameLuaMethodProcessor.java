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

package com.tang.intellij.lua.refactoring.rename;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.util.Query;
import com.tang.intellij.lua.psi.LuaClassMethod;
import com.tang.intellij.lua.psi.search.LuaOverridingMethodsSearch;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 *
 * Created by tangzx on 2017/3/29.
 */
public class RenameLuaMethodProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(@NotNull PsiElement psiElement) {
        return psiElement instanceof LuaClassMethod;
    }

    @Override
    public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames, SearchScope scope) {
        LuaClassMethod methodDef = (LuaClassMethod) element;
        Query<LuaClassMethod> search = LuaOverridingMethodsSearch.search(methodDef);
        search.forEach(methodDef1 -> {
            allRenames.put(methodDef1, newName);
        });
    }
}
