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
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.refactoring.LuaRefactoringUtil
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

/**

 * Created by tangzx on 2016/12/25.
 */
open class ClassMemberCompletionProvider : CompletionProvider<CompletionParameters>() {
    protected abstract class HandlerProcessor {
        open fun processLookupString(lookupString: String): String = lookupString
        abstract fun process(element: LuaLookupElement)
    }

    private class InsertHandlerWrapper(val deleteLen: Int, val name:String, val base: InsertHandler<LookupElement>?) : InsertHandler<LookupElement> {
        override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
            base?.handleInsert(insertionContext, lookupElement)
            val startOffset = insertionContext.startOffset - deleteLen
            insertionContext.document.replaceString(startOffset, insertionContext.startOffset - 1, name)
        }
    }

    override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
        val psi = completionParameters.position
        val indexExpr = psi.parent

        if (indexExpr is LuaIndexExpr) {
            val project = indexExpr.project
            val prefixTypeSet = indexExpr.guessParentType(SearchContext(project))
            if (!Ty.isInvalid(prefixTypeSet)) {
                complete(indexExpr, prefixTypeSet, completionResultSet, completionResultSet.prefixMatcher, null)
            }
            //smart
            val nameExpr = PsiTreeUtil.getChildOfType(indexExpr, LuaNameExpr::class.java)
            if (nameExpr != null) {
                val colon = if (indexExpr.colon != null) ":" else "."
                val nameText = nameExpr.text
                val matcher = CamelHumpMatcher(nameText)
                LuaPsiTreeUtil.walkUpLocalNameDef(indexExpr, {
                    val txt = it.text
                    if (nameText != txt && matcher.prefixMatches(txt)) {
                        val typeSet = it.guessTypeFromCache(SearchContext(project))
                        if (!Ty.isInvalid(prefixTypeSet)) {
                            val prefixMatcher = completionResultSet.prefixMatcher
                            val resultSet = completionResultSet.withPrefixMatcher(prefixMatcher.prefix)
                            complete(indexExpr, typeSet, resultSet, prefixMatcher, object : HandlerProcessor() {
                                override fun process(element: LuaLookupElement) {
                                    element.itemText = txt + colon + element.itemText
                                    element.handler = InsertHandlerWrapper(nameExpr.textLength + 1, txt, element.handler)
                                }
                            })
                        }
                    }
                    true
                })
            }
        }
    }

    private fun complete(indexExpr: LuaIndexExpr, prefixTypeSet: ITy, completionResultSet: CompletionResultSet, prefixMatcher: PrefixMatcher, handlerProcessor: HandlerProcessor?) {
        val isColon = indexExpr.colon != null
        TyUnion.each(prefixTypeSet) { luaType ->
            if (luaType is ITyClass)
                addClass(luaType, indexExpr.project, isColon, completionResultSet, prefixMatcher, handlerProcessor)
        }
    }

    protected fun addClass(luaType:ITyClass, project: Project, isColon:Boolean, completionResultSet: CompletionResultSet, prefixMatcher: PrefixMatcher, handlerProcessor: HandlerProcessor?) {
        val context = SearchContext(project)
        luaType.lazyInit(context)
        luaType.processMembers(context) { curType, def ->
            def.name?.let {
                if (prefixMatcher.prefixMatches(it)) {
                    val className = curType.displayName
                    if (def is LuaClassField && !isColon) {
                        addField(completionResultSet, curType === luaType, className, def, handlerProcessor)
                    } else if (def is LuaClassMethod) {
                        addMethod(completionResultSet, curType === luaType, className, def, handlerProcessor)
                    }
                }
            }
        }
    }

    protected fun addField(completionResultSet: CompletionResultSet, bold: Boolean, clazzName: String, field: LuaClassField, handlerProcessor: HandlerProcessor?) {
        val name = field.name
        if (name != null) {
            val elementBuilder = LuaFieldLookupElement(name, field, bold)
            if (!LuaRefactoringUtil.isLuaIdentifier(name)) {
                elementBuilder.lookupString = "['$name']"
                val baseHandler = elementBuilder.handler
                elementBuilder.handler = InsertHandler<LookupElement> { insertionContext, lookupElement ->
                    baseHandler.handleInsert(insertionContext, lookupElement)
                    // remove '.'
                    insertionContext.document.deleteString(insertionContext.startOffset - 1, insertionContext.startOffset)
                }
            }
            elementBuilder.setTailText("  [$clazzName]")
            handlerProcessor?.process(elementBuilder)
            completionResultSet.addElement(elementBuilder)
        }
    }

    protected fun addMethod(completionResultSet: CompletionResultSet, bold: Boolean, clazzName: String, def: LuaClassMethod, handlerProcessor: HandlerProcessor?) {
        val methodName = def.name
        if (methodName != null) {
            val ty = def.asTy(SearchContext(def.project))
            ty.process(Processor {
                val lookupString = handlerProcessor?.processLookupString(methodName) ?: methodName
                val le = TyFunctionLookupElement(lookupString, def, it, bold, ty, LuaIcons.CLASS_METHOD)
                le.handler = SignatureInsertHandler(it)
                if (!ty.isSelfCall) le.setItemTextUnderlined(true)
                le.setTailText("  [$clazzName]")
                handlerProcessor?.process(le)
                completionResultSet.addElement(le)
                true
            })
        }
    }
}
