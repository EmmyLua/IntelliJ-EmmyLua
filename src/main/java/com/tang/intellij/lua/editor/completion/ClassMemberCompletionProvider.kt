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
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaClassMethodDef
import com.tang.intellij.lua.psi.LuaIndexExpr
import com.tang.intellij.lua.psi.LuaPsiImplUtil
import com.tang.intellij.lua.search.SearchContext

/**

 * Created by tangzx on 2016/12/25.
 */
class ClassMemberCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
        val element = completionParameters.position
        val parent = element.parent

        if (parent is LuaIndexExpr) {
            val indexExpr = parent
            val prefixTypeSet = indexExpr.guessPrefixType(SearchContext(indexExpr.project))
            if (prefixTypeSet != null) {
                if (indexExpr.colon != null) {
                    prefixTypeSet.types.forEach { luaType ->
                        val context = SearchContext(indexExpr.getProject())
                        luaType.initAliasName(context)
                        luaType.processMethods(context) { curType, def ->
                            val className = curType.displayName
                            addMethod(completionResultSet, curType === luaType, false, className, def)
                        }
                    }
                } else {
                    prefixTypeSet.types.forEach { luaType ->
                        val context = SearchContext(indexExpr.getProject())
                        luaType.initAliasName(context)
                        luaType.processMethods(context) { curType, def ->
                            val className = curType.displayName
                            addMethod(completionResultSet, curType === luaType, true, className, def)
                        }
                        luaType.processFields(context) { curType, field ->
                            val className = curType.displayName
                            addField(completionResultSet, curType === luaType, className, field)
                        }
                        luaType.processStaticMethods(context) { curType, def -> addStaticMethod(completionResultSet, curType === luaType, curType.displayName, def) }
                    }
                }
            }
        }
    }

    private fun addField(completionResultSet: CompletionResultSet, bold: Boolean, clazzName: String, field: LuaClassField) {
        val name = field.fieldName
        if (name != null && completionResultSet.prefixMatcher.prefixMatches(name)) {
            val elementBuilder = LuaFieldLookupElement(name, field, bold)
            elementBuilder.setTailText("  [$clazzName]")
            completionResultSet.addElement(elementBuilder)
        }
    }

    private fun addMethod(completionResultSet: CompletionResultSet, bold: Boolean, useAsField: Boolean, clazzName: String, def: LuaClassMethodDef) {
        val methodName = def.name
        if (methodName != null && completionResultSet.prefixMatcher.prefixMatches(methodName)) {
            if (useAsField) {
                var elementBuilder = LookupElementBuilder.create(methodName)
                        .withIcon(LuaIcons.CLASS_METHOD)
                        .withTailText(def.paramSignature + "  [" + clazzName + "]")
                if (bold)
                    elementBuilder = elementBuilder.bold()
                completionResultSet.addElement(elementBuilder)
            } else {
                LuaPsiImplUtil.processOptional(def.params) { signature, mask ->
                    val elementBuilder = LuaMethodLookupElement(methodName, signature, bold, def)
                    elementBuilder.setHandler(FuncInsertHandler(def).withMask(mask))
                    elementBuilder.setTailText("  [$clazzName]")
                    completionResultSet.addElement(elementBuilder)
                }
            }
        }
    }

    private fun addStaticMethod(completionResultSet: CompletionResultSet, bold: Boolean, clazzName: String, def: LuaClassMethodDef) {
        val methodName = def.name
        if (methodName != null && completionResultSet.prefixMatcher.prefixMatches(methodName)) {
            LuaPsiImplUtil.processOptional(def.params) { signature, mask ->
                val elementBuilder = LuaMethodLookupElement(methodName, signature, bold, def)
                elementBuilder.setHandler(FuncInsertHandler(def).withMask(mask))
                elementBuilder.setItemTextUnderlined(true)
                elementBuilder.setTailText("  [$clazzName]")
                completionResultSet.addElement(elementBuilder)
            }
        }
    }
}
