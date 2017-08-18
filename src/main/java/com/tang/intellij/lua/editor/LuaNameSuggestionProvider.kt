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

package com.tang.intellij.lua.editor

import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.psi.codeStyle.SuggestedNameInfo
import com.intellij.refactoring.rename.NameSuggestionProvider
import com.tang.intellij.lua.psi.LuaNameDef
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITyClass
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyUnion
import java.util.*

/**
 * LuaNameDef implements PsiNameIdentifierOwner

 * Created by TangZX on 2016/12/20.
 */
class LuaNameSuggestionProvider : NameSuggestionProvider {
    override fun getSuggestedNames(psiElement: PsiElement, psiElement1: PsiElement?, set: MutableSet<String>): SuggestedNameInfo? {
        if (psiElement is LuaNameDef) {
            val context = SearchContext(psiElement.getProject())
            val typeSet = psiElement.guessType(context)
            if (!Ty.isInvalid(typeSet)) {
                val classNames = HashSet<String>()

                TyUnion.each(typeSet) { type ->
                    if (type is ITyClass) {
                        var cur: ITyClass? = type
                        while (cur != null) {
                            val className = cur.className
                            if (!cur.isAnonymous)
                                classNames.add(className)
                            cur = cur.getSuperClass(context)
                        }
                    }
                }
                for (className in classNames) {
                    val strings = NameUtil.getSuggestionsByName(className, "", "", false, false, false)
                    set.addAll(strings)
                }
            }
        }
        return null
    }
}
