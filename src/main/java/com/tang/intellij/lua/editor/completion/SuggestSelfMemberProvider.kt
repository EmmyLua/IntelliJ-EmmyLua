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

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.isVisibleInScope

/**
 * suggest self.xxx
 * Created by TangZX on 2017/4/11.
 */
class SuggestSelfMemberProvider : ClassMemberCompletionProvider() {
    override fun addCompletions(completionParameters: CompletionParameters,
                                processingContext: ProcessingContext,
                                completionResultSet: CompletionResultSet) {
        val position = completionParameters.position
        val methodDef = PsiTreeUtil.getParentOfType(position, LuaClassMethodDef::class.java)
        if (methodDef != null && !methodDef.isStatic) {
            val project = position.project
            val searchContext = SearchContext(project)
            methodDef.guessClassType(searchContext)?.let { type ->
                val contextTy = LuaPsiTreeUtil.findContextClass(position)
                type.processMembers(searchContext) { curType, member ->
                    if (curType.isVisibleInScope(project, contextTy, member.visibility)) {
                        if (member is LuaClassField) {
                            addField(completionResultSet, curType === type, curType.className, member, object : HandlerProcessor() {
                                override fun process(element: LuaLookupElement): LookupElement {
                                    element.lookupString = "self.${member.name}"
                                    return element
                                }
                            })
                        } else if (member is LuaClassMethod) {
                            addMethod(completionResultSet, curType === type, curType.className, member,  object : HandlerProcessor() {
                                override fun process(element: LuaLookupElement): LookupElement { return element }

                                override fun processLookupString(lookupString: String): String {
                                    return "self:${member.name}"
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}