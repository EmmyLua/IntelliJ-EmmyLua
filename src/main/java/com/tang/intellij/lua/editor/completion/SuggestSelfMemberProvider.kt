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
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaClassMethod
import com.tang.intellij.lua.psi.LuaClassMethodDef
import com.tang.intellij.lua.psi.LuaPsiImplUtil
import com.tang.intellij.lua.search.SearchContext

/**
 * suggest self.xxx
 * Created by TangZX on 2017/4/11.
 */
class SuggestSelfMemberProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(completionParameters: CompletionParameters,
                                processingContext: ProcessingContext,
                                completionResultSet: CompletionResultSet) {
        val position = completionParameters.position
        val methodDef = PsiTreeUtil.getParentOfType(position, LuaClassMethodDef::class.java)
        if (methodDef != null && !methodDef.isStatic) {
            val searchContext = SearchContext(position.project)
            val type = methodDef.getClassType(searchContext)
            type?.processMembers(searchContext) { curType, member ->
                if (member is LuaClassField) {
                    val fieldName = member.fieldName
                    if (fieldName != null) {
                        val elementBuilder = LuaFieldLookupElement("self." + fieldName, member, curType === type)
                        elementBuilder.setTailText("  [" + curType.displayName + "]")
                        completionResultSet.addElement(elementBuilder)
                    }
                } else if (member is LuaClassMethod) {
                    val methodName = member.name
                    if (methodName != null) {
                        LuaPsiImplUtil.processOptional(member.params) { signature, mask ->
                            val elementBuilder = LuaMethodLookupElement("self:" + methodName, signature, curType === type, member)
                            elementBuilder.handler = FuncInsertHandler(member).withMask(mask)
                            elementBuilder.setTailText("  [" + curType.displayName + "]")
                            completionResultSet.addElement(elementBuilder)
                        }
                    }
                }
            }
        }
    }
}