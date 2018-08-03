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

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.psi.LuaTableExpr
import com.tang.intellij.lua.psi.shouldBe
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy

class TableCompletionProvider : ClassMemberCompletionProvider() {

    companion object {
        private val metaMethodNames = mapOf(
                "__add" to "a + b",
                "__sub" to "a - b",
                "__mul" to "a * b",
                "__div" to "a / b",
                "__mod" to "a % b",
                "__pow" to "a ^ b",
                "__unm" to "-a",
                "__concat" to "a .. b",
                "__len" to "#a",
                "__eq" to "a == a",
                "__lt" to "a < b",
                "__le" to "a <= b",
                "__index" to "Meta method",
                "__newindex" to "Meta method",
                "__call" to "Meta method",
                "__tostring" to "Meta method",
                "__metatable" to "Meta method"
        )
    }
    override fun addCompletions(session: CompletionSession) {
        val completionParameters = session.parameters
        val completionResultSet = session.resultSet
        metaMethodNames.forEach {
            val b = LookupElementBuilder.create(it.key)
                    .withTypeText(it.value)
                    .withIcon(LuaIcons.META_METHOD)
            completionResultSet.addElement(b)
        }

        val table = PsiTreeUtil.getParentOfType(completionParameters.position, LuaTableExpr::class.java)
        if (table != null) {
            val project = table.project
            val prefixMatcher = completionResultSet.prefixMatcher
            val ty = table.shouldBe(SearchContext(project))
            ty.eachTopClass(Processor { luaType ->
                val context = SearchContext(project)
                luaType.lazyInit(context)
                luaType.processMembers(context) { curType, member ->
                    member.name?.let {
                        if (prefixMatcher.prefixMatches(it)) {
                            val className = curType.displayName
                            if (member is LuaClassField) {
                                addField(completionResultSet, curType === luaType, className, member, null, object : HandlerProcessor() {
                                    override fun process(element: LuaLookupElement, member: LuaClassMember, memberTy: ITy?): LookupElement {
                                        element.itemText = element.itemText + " = "
                                        element.lookupString = element.lookupString + " = "

                                        return PrioritizedLookupElement.withPriority(element, 10.0)
                                    }
                                })
                            }
                        }
                    }
                }
                true
            })
        }
    }
}