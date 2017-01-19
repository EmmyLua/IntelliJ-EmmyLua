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

import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.refactoring.rename.NameSuggestionProvider;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaNameDef;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LuaNameDef implements PsiNameIdentifierOwner
 *
 * Created by TangZX on 2016/12/20.
 */
public class LuaNameSuggestionProvider implements NameSuggestionProvider {
    @Nullable
    @Override
    public SuggestedNameInfo getSuggestedNames(PsiElement psiElement, @Nullable PsiElement psiElement1, Set<String> set) {
        if (psiElement instanceof LuaNameDef) {
            LuaNameDef nameDef = (LuaNameDef) psiElement;
            SearchContext context = new SearchContext(psiElement.getProject());
            LuaTypeSet typeSet = nameDef.guessType(context);
            if (typeSet != null) {
                Set<String> classNames = new HashSet<>();

                for (LuaType type : typeSet.getTypes()) {
                    LuaType cur = type;
                    while (cur != null) {
                        String className = cur.getClassNameText();
                        if (className != null)
                            classNames.add(className);
                        cur = cur.getSuperClass(context);
                    }
                }

                for (String className : classNames) {
                    List<String> strings = NameUtil.getSuggestionsByName(className, "", "", false, false, false);
                    set.addAll(strings);
                }
            }
        }
        return null;
    }
}
