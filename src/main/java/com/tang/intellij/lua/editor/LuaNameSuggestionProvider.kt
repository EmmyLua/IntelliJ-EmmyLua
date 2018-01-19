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
import com.intellij.psi.PsiReference
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.psi.codeStyle.SuggestedNameInfo
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.rename.NameSuggestionProvider
import com.intellij.util.Processor
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*
import java.util.*

/**
 *
 * Created by TangZX on 2016/12/20.
 */
class LuaNameSuggestionProvider : NameSuggestionProvider {

    companion object {
        fun fixName(oriName: String): String {
            return oriName.replace(".", "")
        }
    }
    
    private fun collectNames(type: ITy, context: SearchContext, collector: (name: String, suffix: String, preferLonger: Boolean) -> Unit) {
        when (type) {
            is ITyClass -> {
                var cur: ITy? = type
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

    //逆向
    private fun getNames(ref: PsiReference, set: MutableSet<String>) {
        val ele = ref.element
        val p1 = ele.parent
        if (ele is LuaExpr) {
            when (p1) {
                is LuaListArgs -> {
                    //call(var)
                    val paramIndex = p1.getIndexFor(ele)
                    val p2 = p1.parent as? LuaCallExpr
                    if (p2 != null) {
                        val ty = p2.guessParentType(SearchContext(ele.project))
                        TyUnion.each(ty) {
                            if (it is ITyFunction) {
                                it.process(Processor { sig ->
                                    sig.params.getOrNull(paramIndex)?.let {
                                        set.add(it.name)
                                    }
                                    return@Processor false
                                })
                            }
                        }
                    }
                }
                is LuaExprList -> {
                    //xxx = var
                    val p2 = p1.parent as? LuaAssignStat
                    val valueList = p2?.valueExprList?.exprList
                    if (valueList != null) {
                        val index = valueList.indexOf(ele)
                        val varExpr = p2.getExprAt(index)
                        if (varExpr is LuaIndexExpr) varExpr.name?.let { set.add(it) }
                    }
                }
            }
        }
    }

    override fun getSuggestedNames(psi: PsiElement, nameSuggestionContext: PsiElement?, set: MutableSet<String>): SuggestedNameInfo? {
        val search = ReferencesSearch.search(psi, psi.useScope)
        search.forEach { getNames(it, set) }

        if (psi is LuaTypeGuessable) {
            val context = SearchContext(psi.getProject())
            val type = psi.guessType(context)
            if (!Ty.isInvalid(type)) {
                val names = HashSet<String>()

                TyUnion.each(type) { ty ->
                    collectNames(ty, context) { name, suffix, preferLonger ->
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
