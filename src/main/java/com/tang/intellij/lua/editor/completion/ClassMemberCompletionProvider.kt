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
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project
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

    override fun addCompletions(completionParameters: CompletionParameters,
                                processingContext: ProcessingContext,
                                completionResultSet: CompletionResultSet) {
        val psi = completionParameters.position
        val indexExpr = psi.parent

        if (indexExpr is LuaIndexExpr) {
            val isColon = indexExpr.colon != null
            val project = indexExpr.project
            val searchContext = SearchContext(project)
            val contextTy = LuaPsiTreeUtil.findContextClass(indexExpr)
            val prefixType = indexExpr.guessParentType(searchContext)
            if (!Ty.isInvalid(prefixType)) {
                complete(isColon, project, contextTy, prefixType, completionResultSet, completionResultSet.prefixMatcher, null)
            }
            //smart
            val nameExpr = indexExpr.prefixExpr
            if (nameExpr is LuaNameExpr) {
                val colon = if (isColon) ":" else "."
                val prefixName = nameExpr.text
                val postfixName = indexExpr.name?.let { it.substring(0, it.indexOf(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED)) }

                val matcher = completionResultSet.prefixMatcher.cloneWithPrefix(prefixName)
                LuaPsiTreeUtilEx.walkUpNameDef(indexExpr, Processor {
                    val txt = it.name
                    if (it is LuaTypeGuessable && txt != null && prefixName != txt && matcher.prefixMatches(txt)) {
                        val type = it.guessTypeFromCache(searchContext)
                        if (!Ty.isInvalid(prefixType)) {
                            val prefixMatcher = completionResultSet.prefixMatcher
                            val resultSet = completionResultSet.withPrefixMatcher("$prefixName*$postfixName")
                            complete(isColon, project, contextTy, type, resultSet, prefixMatcher, object : HandlerProcessor() {
                                override fun process(element: LuaLookupElement) {
                                    element.itemText = txt + colon + element.itemText
                                    element.lookupString = txt + colon + element.lookupString
                                }
                            })
                        }
                    }
                    true
                })
            }
        }
    }

    private fun complete(isColon: Boolean,
                         project: Project,
                         contextTy: ITy,
                         prefixType: ITy,
                         completionResultSet: CompletionResultSet,
                         prefixMatcher: PrefixMatcher,
                         handlerProcessor: HandlerProcessor?) {
        TyUnion.each(prefixType) { luaType ->
            if (luaType is ITyClass)
                addClass(contextTy, luaType, project, isColon, completionResultSet, prefixMatcher, handlerProcessor)
        }
    }

    protected fun addClass(contextTy: ITy,
                           luaType:ITyClass,
                           project: Project,
                           isColon:Boolean,
                           completionResultSet: CompletionResultSet,
                           prefixMatcher: PrefixMatcher,
                           handlerProcessor: HandlerProcessor?) {
        val context = SearchContext(project)
        luaType.lazyInit(context)
        luaType.processMembers(context) { curType, member ->
            member.name?.let {
                if (prefixMatcher.prefixMatches(it) && curType.isVisibleInScope(project, contextTy, member.visibility)) {
                    val className = curType.displayName
                    if (member is LuaClassField && !isColon) {
                        addField(completionResultSet, curType === luaType, className, member, handlerProcessor)
                    } else if (member is LuaClassMethod) {
                        addMethod(completionResultSet, curType === luaType, className, member, handlerProcessor)
                    }
                }
            }
        }
    }

    protected fun addField(completionResultSet: CompletionResultSet,
                           bold: Boolean,
                           clazzName: String,
                           field: LuaClassField,
                           handlerProcessor: HandlerProcessor?) {
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

    protected fun addMethod(completionResultSet: CompletionResultSet,
                            bold: Boolean,
                            clazzName: String,
                            classMethod: LuaClassMethod,
                            handlerProcessor: HandlerProcessor?) {
        val methodName = classMethod.name
        if (methodName != null) {
            val ty = classMethod.guessType(SearchContext(classMethod.project)) as ITyFunction
            ty.process(Processor {
                val lookupString = handlerProcessor?.processLookupString(methodName) ?: methodName
                val le = TyFunctionLookupElement(lookupString, classMethod, it, bold, ty, classMethod.visibility.warpIcon(LuaIcons.CLASS_METHOD))
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
