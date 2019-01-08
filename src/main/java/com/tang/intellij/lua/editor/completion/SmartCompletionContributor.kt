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

package com.tang.intellij.lua.editor.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.LuaNameExpr
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.psi.shouldBe
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.TyAliasSubstitutor
import com.tang.intellij.lua.ty.TyStringLiteral

class SmartCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, EXPR, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                val id = completionParameters.position
                val expr = PsiTreeUtil.getParentOfType(id, LuaNameExpr::class.java) ?: return
                val ty = expr.shouldBe(SearchContext(expr.project)).substitute(TyAliasSubstitutor(expr.project))
                ty.each {
                    if (it is TyStringLiteral) {
                        val lookupElement = LookupElementBuilder.create(it.content)
                                .withLookupString(it.content)
                                .withIcon(LuaIcons.STRING_LITERAL)
                        completionResultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, 111111999.0))
                    }
                }
            }
        })
    }
    companion object {
        private val EXPR = PlatformPatterns.psiElement(LuaTypes.ID).withParent(LuaNameExpr::class.java)
    }
}