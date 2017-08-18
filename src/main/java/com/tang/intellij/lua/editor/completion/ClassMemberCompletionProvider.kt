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
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.refactoring.LuaRefactoringUtil
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyClass
import com.tang.intellij.lua.ty.TyUnion

/**

 * Created by tangzx on 2016/12/25.
 */
class ClassMemberCompletionProvider : CompletionProvider<CompletionParameters>() {
    private interface HandlerProcessor {
        fun process(element: LuaLookupElement)
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
        val parent = psi.parent

        if (parent is LuaIndexExpr) {
            val indexExpr = parent
            val project = indexExpr.project
            val prefixTypeSet = indexExpr.guessPrefixType(SearchContext(project))
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
                        val typeSet = it.guessType(SearchContext(project))
                        if (!Ty.isInvalid(prefixTypeSet)) {
                            val prefixMatcher = completionResultSet.prefixMatcher
                            val resultSet = completionResultSet.withPrefixMatcher(prefixMatcher.prefix)
                            complete(indexExpr, typeSet, resultSet, prefixMatcher, object : HandlerProcessor {
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

    private fun complete(indexExpr: LuaIndexExpr, prefixTypeSet: Ty, completionResultSet: CompletionResultSet, prefixMatcher: PrefixMatcher, handlerProcessor: HandlerProcessor?) {
        if (indexExpr.colon != null) {
            TyUnion.each(prefixTypeSet) { luaType ->
                if (luaType is TyClass) {
                    val context = SearchContext(indexExpr.project)
                    luaType.initAliasName(context)
                    luaType.processMethods(context) { curType, def ->
                        val className = curType.displayName
                        addMethod(completionResultSet, prefixMatcher, curType === luaType, false, className, def, handlerProcessor)
                    }
                }
            }
        } else {
            TyUnion.each(prefixTypeSet)  { luaType ->
                if (luaType is TyClass) {
                    val context = SearchContext(indexExpr.project)
                    luaType.initAliasName(context)
                    luaType.processMethods(context) { curType, def ->
                        val className = curType.displayName
                        addMethod(completionResultSet, prefixMatcher, curType === luaType, true, className, def, handlerProcessor)
                    }
                    luaType.processFields(context) { curType, field ->
                        val className = curType.displayName
                        addField(completionResultSet, prefixMatcher, curType === luaType, className, field, handlerProcessor)
                    }
                    luaType.processStaticMethods(context) { curType, def ->
                        addStaticMethod(completionResultSet, prefixMatcher, curType === luaType, curType.displayName, def, handlerProcessor)
                    }
                }
            }
        }
    }

    private fun addField(completionResultSet: CompletionResultSet, prefixMatcher: PrefixMatcher, bold: Boolean, clazzName: String, field: LuaClassField, handlerProcessor: HandlerProcessor?) {
        val name = field.fieldName
        if (name != null && prefixMatcher.prefixMatches(name)) {
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

    private fun addMethod(completionResultSet: CompletionResultSet, prefixMatcher: PrefixMatcher, bold: Boolean, useAsField: Boolean, clazzName: String, def: LuaClassMethod, handlerProcessor: HandlerProcessor?) {
        val methodName = def.name
        if (methodName != null && prefixMatcher.prefixMatches(methodName)) {
            if (useAsField) {
                val elementBuilder = LuaLookupElement(methodName, bold, LuaIcons.CLASS_METHOD)
                elementBuilder.setTailText(def.paramSignature + "  [" + clazzName + "]")
                handlerProcessor?.process(elementBuilder)
                completionResultSet.addElement(elementBuilder)
            } else {
                LuaPsiImplUtil.processOptional(def.params) { signature, mask ->
                    val elementBuilder = LuaMethodLookupElement(methodName, signature, bold, def)
                    elementBuilder.handler = FuncInsertHandler(def).withMask(mask)
                    elementBuilder.setTailText("  [$clazzName]")
                    handlerProcessor?.process(elementBuilder)
                    completionResultSet.addElement(elementBuilder)
                }
            }
        }
    }

    private fun addStaticMethod(completionResultSet: CompletionResultSet, prefixMatcher: PrefixMatcher, bold: Boolean, clazzName: String, def: LuaClassMethod, handlerProcessor: HandlerProcessor?) {
        val methodName = def.name
        if (methodName != null && prefixMatcher.prefixMatches(methodName)) {
            LuaPsiImplUtil.processOptional(def.params) { signature, mask ->
                val elementBuilder = LuaMethodLookupElement(methodName, signature, bold, def)
                elementBuilder.handler = FuncInsertHandler(def).withMask(mask)
                elementBuilder.setItemTextUnderlined(true)
                elementBuilder.setTailText("  [$clazzName]")
                handlerProcessor?.process(elementBuilder)
                completionResultSet.addElement(elementBuilder)
            }
        }
    }
}
