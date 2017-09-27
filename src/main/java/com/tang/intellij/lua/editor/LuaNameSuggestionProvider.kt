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
import com.tang.intellij.lua.psi.LuaTypeGuessable
import com.tang.intellij.lua.psi.guessTypeFromCache
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*
import java.util.*

/**
 *
 * Created by TangZX on 2016/12/20.
 */
class LuaNameSuggestionProvider : NameSuggestionProvider {

    private fun fixName(oriName: String): String {
        return oriName.replace(".", "")
    }

    private fun collectNames(type: ITy, context: SearchContext, collector: (name: String, suffix: String, preferLonger: Boolean) -> Unit) {
        when (type) {
            is ITyClass -> {
                var cur: ITyClass? = type
                while (cur != null) {
                    if (!cur.isAnonymous)
                        collector(fixName(type.className), "", false)
                    cur = cur.getSuperClass(context)
                }
            }
            is ITyArray -> collectNames(type.base, context) { name, _, _ ->
                collector(name, "List", false)
            }
            is ITyGeneric -> {
                val paramTy = type.getParamTy(1)
                collectNames(paramTy, context) { name, _, _ ->
                    collector(name, "Map", false)
                }
            }
        }
    }

    override fun getSuggestedNames(psiElement: PsiElement, nameSuggestionContext: PsiElement?, set: MutableSet<String>): SuggestedNameInfo? {
        if (psiElement is LuaTypeGuessable) {
            val context = SearchContext(psiElement.getProject())
            val typeSet = psiElement.guessTypeFromCache(context)
            if (!Ty.isInvalid(typeSet)) {
                val names = HashSet<String>()

                TyUnion.each(typeSet) { type ->
                    collectNames(type, context) { name, suffix, preferLonger ->
                        if (names.add(name)) {
                            val strings = NameUtil.getSuggestionsByName(name, "", suffix, false, preferLonger, false)
                            set.addAll(strings)
                        }
                    }
                }
            }
        }
        return null
    }
}
