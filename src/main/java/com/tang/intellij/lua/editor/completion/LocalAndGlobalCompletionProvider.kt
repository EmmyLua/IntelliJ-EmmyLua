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
import com.intellij.psi.tree.TokenSet
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.highlighting.LuaSyntaxHighlighter
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaGlobalIndex
import com.tang.intellij.lua.ty.*

/**
 * suggest local/global vars and functions
 * Created by TangZX on 2017/4/11.
 */
class LocalAndGlobalCompletionProvider internal constructor(private val mask: Int) : ClassMemberCompletionProvider() {

    private fun has(flag: Int): Boolean {
        return mask and flag == flag
    }

    private fun addCompletion(name:String, session: CompletionSession, psi: LuaPsiElement) {
        val addTy = {ty: ITyFunction ->
            val icon = if (ty.isGlobal)
                LuaIcons.GLOBAL_FUNCTION
            else
                LuaIcons.LOCAL_FUNCTION

            ty.process(Processor {
                val le = TyFunctionLookupElement(name, psi, it, false, ty, icon)
                le.handler = SignatureInsertHandler(it)
                session.resultSet.addElement(le)
                true
            })
        }
        when (psi) {
            is LuaFuncBodyOwner -> {
                addTy(psi.guessType(SearchContext(psi.project)))
            }
            is LuaTypeGuessable -> {
                val type = psi.guessTypeFromCache(SearchContext(psi.project))
                var isFn = false
                TyUnion.each(type) {
                    if (it is ITyFunction) {
                        isFn = true
                        addTy(it)
                    }
                }
                if (!isFn) {
                    val cls = TyUnion.getPerfectClass(type)
                    if (cls != null) {
                        var icon = LuaIcons.LOCAL_VAR
                        if (psi is LuaParamNameDef)
                            icon = LuaIcons.PARAMETER

                        val elementBuilder = LuaTypeGuessableLookupElement(name, psi, cls, false, icon)
                        session.resultSet.addElement(elementBuilder)
                    }
                }
            }
        }
    }

    override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
        val session = completionParameters.editor.getUserData(CompletionSession.KEY)!!
        val cur = completionParameters.position
        val nameExpr = cur.parent

        //module members
        if (nameExpr is LuaNameExpr) {
            val moduleName = nameExpr.moduleName
            if (moduleName != null) {
                val ty = TyLazyClass(moduleName)
                val contextTy = LuaPsiTreeUtil.findContextClass(nameExpr)
                addClass(contextTy, ty, cur.project, true, completionResultSet, completionResultSet.prefixMatcher, null)
            }
        }

        //local names
        val localNamesSet = mutableSetOf<String>()

        //local
        if (has(LOCAL_VAR)) {
            LuaPsiTreeUtil.walkUpLocalNameDef(cur) { nameDef ->
                val name = nameDef.text
                if (completionResultSet.prefixMatcher.prefixMatches(name) && localNamesSet.add(name)) {
                    session.addWord(name)
                    addCompletion(name, session, nameDef)
                }
                true
            }
        }
        if (has(LOCAL_FUN)) {
            LuaPsiTreeUtil.walkUpLocalFuncDef(cur) { localFuncDef ->
                val name = localFuncDef.name
                if (name != null && completionResultSet.prefixMatcher.prefixMatches(name) && localNamesSet.add(name)) {
                    session.addWord(name)
                    addCompletion(name, session, localFuncDef)
                }
                true
            }
        }

        //global
        val project = cur.project
        if (has(GLOBAL_FUN) || has(GLOBAL_VAR)) {
            val context = SearchContext(project)
            val names = mutableListOf<String>()
            LuaGlobalIndex.instance.processAllKeys(project) { name ->
                if (completionResultSet.prefixMatcher.prefixMatches(name) && localNamesSet.add(name)) {
                    names.add(name)
                }
                true
            }
            names.forEach { name ->
                val global = LuaGlobalIndex.find(name, context)
                if (global != null) {
                    session.addWord(name)
                    addCompletion(name, session, global)
                }
            }
        }
        //key words
        if (has(KEY_WORDS)) {
            for (keyWordToken in KEYWORD_TOKENS.types) {
                session.addWord(keyWordToken.toString())

                completionResultSet.addElement(LookupElementBuilder.create(keyWordToken)
                        .withInsertHandler(KeywordInsertHandler(keyWordToken))
                )
            }
            for (primitiveToken in LuaSyntaxHighlighter.PRIMITIVE_TYPE_SET.types) {
                session.addWord(primitiveToken.toString())
                completionResultSet.addElement(LookupElementBuilder.create(primitiveToken))
            }
            completionResultSet.addElement(LookupElementBuilder.create(Constants.WORD_SELF))
        }
    }

    companion object {

        private val LOCAL_VAR = 1
        private val LOCAL_FUN = 2
        private val GLOBAL_VAR = 4
        private val GLOBAL_FUN = 8
        private val KEY_WORDS = 16

        @JvmStatic val ALL = LOCAL_VAR or LOCAL_FUN or GLOBAL_VAR or GLOBAL_FUN or KEY_WORDS
        @JvmStatic val VARS = LOCAL_VAR or GLOBAL_VAR

        private val KEYWORD_TOKENS = TokenSet.create(
                LuaTypes.AND,
                LuaTypes.BREAK,
                LuaTypes.DO,
                LuaTypes.ELSE,
                //LuaTypes.ELSEIF,
                LuaTypes.END,
                //LuaTypes.FOR,
                LuaTypes.FUNCTION,
                //LuaTypes.IF,
                LuaTypes.IN,
                LuaTypes.LOCAL,
                LuaTypes.NOT,
                LuaTypes.OR,
                LuaTypes.REPEAT,
                LuaTypes.RETURN,
                LuaTypes.THEN,
                LuaTypes.UNTIL,
                LuaTypes.WHILE,

                //Lua5.3
                LuaTypes.GOTO,
                LuaTypes.DOUBLE_COLON
        )
    }
}