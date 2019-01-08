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

import com.google.common.collect.LinkedHashMultimap
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.Processor
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaIndexExprType
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.ty.*

enum class MemberCompletionMode {
    Dot,    // self.xxx
    Colon,  // self:xxx()
    All     // self.xxx && self:xxx()
}

/**

 * Created by tangzx on 2016/12/25.
 */
open class ClassMemberCompletionProvider : LuaCompletionProvider() {
    protected abstract class HandlerProcessor {
        open fun processLookupString(lookupString: String, member: LuaClassMember, memberTy: ITy?): String = lookupString
        abstract fun process(element: LuaLookupElement, member: LuaClassMember, memberTy: ITy?): LookupElement
    }

    override fun addCompletions(session: CompletionSession) {
        val completionParameters = session.parameters
        val completionResultSet = session.resultSet

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

            // for chain
            // 多级t.data.name的提示
            // 查找并t.data占位类型，并扩展到结果中
            // val list = LuaIndexExprType.getAllKnownIndexLuaExprType(indexExpr, searchContext)
            LuaIndexExprType.getAllKnownIndexLuaExprType(indexExpr, searchContext).forEach {
                val baseType = it.key
                val indexExprNames = it.value
                TyUnion.each(baseType, {
                    if (it is ITyClass) {
                        // it类型是：118@F_LuaTest_src_test_t.lua
                        // 获取118@F_LuaTest_src_test_t.lua.__data类型
                        val parentClassNameOfCurrentIndexExpr = LuaIndexExprType.getFiledNameAsClassName(it.className, indexExprNames.toTypedArray(), indexExprNames.size)

                        val all = LuaClassMemberIndex.instance.get(parentClassNameOfCurrentIndexExpr.hashCode(), searchContext.project, searchContext.getScope())
                        val suggestDotCountMap = LinkedHashMultimap.create<Int, LuaIndexExpr>()
                        val inputText = indexExpr.text.replace(".$DUMMY_IDENTIFIER_TRIMMED", "") // t.data.
                        all.forEach {
                            val completeText = it.text  // t.data.name.a.b
                            if (it is LuaIndexExpr) {
                                suggestDotCountMap.put(completeText.replace(inputText, "").count { it == '.' }, it)
                            }
                        }

                        suggestDotCountMap.keys().sorted().forEach {
                            suggestDotCountMap.get(it).forEach {
                                var suggestText = it.text.replaceFirst(inputText, "")
                                if (suggestText[0] == '.') {
                                    suggestText = suggestText.substring(1)
                                }

                                val funType = it.guessType(searchContext)
                                if (funType is ITyFunction) {
                                    val luaIndexExpr = it as LuaIndexExpr
                                    val funName = it.name!!
                                    resolveTypeByRoot(it, it.name!!, searchContext).forEach {
                                        // 遍历所有方法
                                        funType.process(Processor {
                                            val element = TyFunctionLookupElement(funName,
                                                    luaIndexExpr,
                                                    it,
                                                    true,
                                                    isColon,
                                                    funType,
                                                    LuaIcons.CLASS_METHOD)

                                            element.handler = SignatureInsertHandler(it, isColon)
                                            element.setTailText("  [${funName}]")
                                            completionResultSet.addElement(element)
                                            true
                                        })
                                    }
                                } else {
                                    if (!isColon) {
                                        val element = LookupElementFactory.createGuessableLookupElement(suggestText, it, funType, LuaIcons.CLASS_FIELD) as LuaTypeGuessableLookupElement
                                        completionResultSet.addElement(element)
                                    }
                                }
                            }
                        }
                    }
                })
            }
            // end

            //smart
            val nameExpr = indexExpr.prefixExpr
            if (nameExpr is LuaNameExpr) {
                val colon = if (isColon) ":" else "."
                val prefixName = nameExpr.text
                val postfixName = indexExpr.name?.let { it.substring(0, it.indexOf(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED)) }

                val matcher = completionResultSet.prefixMatcher.cloneWithPrefix(prefixName)
                LuaDeclarationTree.get(indexExpr.containingFile).walkUpLocal(indexExpr) { d ->
                    val it = d.firstDeclaration.psi
                    val txt = it.name
                    if (it is LuaTypeGuessable && txt != null && prefixName != txt && matcher.prefixMatches(txt)) {
                        val type = it.guessType(searchContext)
                        if (!Ty.isInvalid(prefixType)) {
                            val prefixMatcher = completionResultSet.prefixMatcher
                            val resultSet = completionResultSet.withPrefixMatcher("$prefixName*$postfixName")
                            complete(isColon, project, contextTy, type, resultSet, prefixMatcher, object : HandlerProcessor() {
                                override fun process(element: LuaLookupElement, member: LuaClassMember, memberTy: ITy?): LookupElement {
                                    element.itemText = txt + colon + element.itemText
                                    element.lookupString = txt + colon + element.lookupString
                                    return PrioritizedLookupElement.withPriority(element, -2.0)
                                }
                            })
                        }
                    }
                    true
                }
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
        val mode = if (isColon) MemberCompletionMode.Colon else MemberCompletionMode.Dot
        prefixType.eachTopClass(Processor { luaType ->
            addClass(contextTy, luaType, project, mode, completionResultSet, prefixMatcher, handlerProcessor)
            true
        })
    }

    protected fun addClass(contextTy: ITy,
                           luaType:ITyClass,
                           project: Project,
                           completionMode:MemberCompletionMode,
                           completionResultSet: CompletionResultSet,
                           prefixMatcher: PrefixMatcher,
                           handlerProcessor: HandlerProcessor?) {
        val context = SearchContext(project)
        luaType.lazyInit(context)
        luaType.processMembers(context) { curType, member ->
            ProgressManager.checkCanceled()
            member.name?.let {
                if (prefixMatcher.prefixMatches(it) && curType.isVisibleInScope(project, contextTy, member.visibility)) {
                    addMember(completionResultSet,
                            member,
                            curType,
                            luaType,
                            completionMode,
                            project,
                            handlerProcessor)
                }
            }
        }
    }

    protected fun addMember(completionResultSet: CompletionResultSet,
                            member: LuaClassMember,
                            thisType: ITyClass,
                            callType: ITyClass,
                            completionMode: MemberCompletionMode,
                            project: Project,
                            handlerProcessor: HandlerProcessor?) {
        val type = member.guessType(SearchContext(project))
        val bold = thisType == callType
        val className = thisType.displayName
        if (type is ITyFunction) {
            addFunction(completionResultSet, bold, completionMode != MemberCompletionMode.Dot, className, member, type, thisType, callType, handlerProcessor)
        } else if (member is LuaClassField) {
            if (completionMode != MemberCompletionMode.Colon)
                addField(completionResultSet, bold, className, member, type, handlerProcessor)
        }
    }

    protected fun addField(completionResultSet: CompletionResultSet,
                           bold: Boolean,
                           clazzName: String,
                           field: LuaClassField,
                           ty:ITy?,
                           handlerProcessor: HandlerProcessor?) {
        val name = field.name
        if (name != null) {
            val element = LookupElementFactory.createFieldLookupElement(clazzName, name, field, ty, bold)
            val ele = handlerProcessor?.process(element, field, null) ?: element
            completionResultSet.addElement(ele)
        }
    }

    private fun addFunction(completionResultSet: CompletionResultSet,
                            bold: Boolean,
                            isColonStyle: Boolean,
                            clazzName: String,
                            classMember: LuaClassMember,
                            fnTy: ITyFunction,
                            thisType: ITyClass,
                            callType: ITyClass,
                            handlerProcessor: HandlerProcessor?) {
        val name = classMember.name
        if (name != null) {
            fnTy.process(Processor {

                val firstParam = it.getFirstParam(thisType, isColonStyle)
                if (isColonStyle) {
                    if (firstParam == null) return@Processor true
                    if (!callType.subTypeOf(firstParam.ty, SearchContext(classMember.project), true))
                        return@Processor true
                }

                val lookupString = handlerProcessor?.processLookupString(name, classMember, fnTy) ?: name

                val element = LookupElementFactory.createMethodLookupElement(clazzName,
                        lookupString,
                        classMember,
                        it,
                        bold,
                        isColonStyle,
                        fnTy,
                        LuaIcons.CLASS_METHOD)
                val ele = handlerProcessor?.process(element, classMember, fnTy) ?: element
                completionResultSet.addElement(ele)
                true
            })
        }
    }
}
