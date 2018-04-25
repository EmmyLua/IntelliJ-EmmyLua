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
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.TyClass
import com.tang.intellij.lua.ty.TyLazyClass

/**
 * override supper
 * Created by tangzx on 2016/12/25.
 */
class OverrideCompletionProvider : LuaCompletionProvider() {
    override fun addCompletions(session: CompletionSession) {
        val completionParameters = session.parameters
        val completionResultSet = session.resultSet
        val id = completionParameters.position
        val methodDef = PsiTreeUtil.getParentOfType(id, LuaClassMethodDef::class.java)
        if (methodDef != null) {
            val context = SearchContext(methodDef.project)
            val classType = methodDef.guessClassType(context)
            if (classType != null) {
                val sup = classType.getSuperClass(context)
                val memberNameSet = mutableSetOf<String>()
                classType.processMembers(context, { _, m ->
                    m.name?.let { memberNameSet.add(it) }
                }, false)
                addOverrideMethod(completionParameters, completionResultSet, memberNameSet, sup)
            }
        }
    }

    private fun addOverrideMethod(completionParameters: CompletionParameters, completionResultSet: CompletionResultSet, memberNameSet:MutableSet<String>, sup: ITy?) {
        var superCls = sup
        if (superCls != null && superCls is TyClass) {
            val project = completionParameters.originalFile.project
            val context = SearchContext(project)
            val clazzName = superCls.className
            LuaClassMemberIndex.processAll(TyLazyClass(clazzName), context, Processor { def ->
                if (def is LuaClassMethod) {
                    def.name?.let {
                        if (memberNameSet.add(it)) {
                            val elementBuilder = LookupElementBuilder.create(def.name!!)
                                    .withIcon(LuaIcons.CLASS_METHOD_OVERRIDING)
                                    .withInsertHandler(OverrideInsertHandler(def))
                                    .withTailText(def.paramSignature)

                            completionResultSet.addElement(elementBuilder)
                        }
                    }
                }
                true
            })

            superCls = superCls.getSuperClass(context)
            addOverrideMethod(completionParameters, completionResultSet, memberNameSet, superCls)
        }
    }

    internal class OverrideInsertHandler(funcBodyOwner: LuaFuncBodyOwner) : FuncInsertHandler(funcBodyOwner) {

        override val autoInsertParameters = true

        override fun createTemplate(manager: TemplateManager, paramNameDefList: Array<LuaParamInfo>): Template {
            val template = super.createTemplate(manager, paramNameDefList)
            template.addEndVariable()
            template.addTextSegment("\nend")
            return template
        }
    }
}
