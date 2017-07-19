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
import com.intellij.psi.tree.TokenSet
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.highlighting.LuaSyntaxHighlighter
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.LuaParamNameDef
import com.tang.intellij.lua.psi.LuaPsiImplUtil
import com.tang.intellij.lua.psi.LuaPsiTreeUtil
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaGlobalFuncIndex
import com.tang.intellij.lua.stubs.index.LuaGlobalVarIndex

/**
 * suggest local/global vars and functions
 * Created by TangZX on 2017/4/11.
 */
class LocalAndGlobalCompletionProvider internal constructor(private val mask: Int) : CompletionProvider<CompletionParameters>() {

    private fun has(flag: Int): Boolean {
        return mask and flag == flag
    }

    override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
        val session = completionParameters.editor.getUserData(CompletionSession.KEY)!!

        //local
        val cur = completionParameters.position
        if (has(LOCAL_VAR)) {
            LuaPsiTreeUtil.walkUpLocalNameDef(cur) { nameDef ->
                val name = nameDef.text
                if (completionResultSet.prefixMatcher.prefixMatches(name)) {
                    session.addWord(name)
                    var icon = LuaIcons.LOCAL_VAR
                    if (nameDef is LuaParamNameDef)
                        icon = LuaIcons.PARAMETER

                    val elementBuilder = LuaTypeGuessableLookupElement(name, nameDef, false, icon)
                    completionResultSet.addElement(elementBuilder)
                }
                true
            }
        }
        if (has(LOCAL_FUN)) {
            LuaPsiTreeUtil.walkUpLocalFuncDef(cur) { localFuncDef ->
                val name = localFuncDef.name
                if (name != null && completionResultSet.prefixMatcher.prefixMatches(name)) {
                    session.addWord(name)
                    LuaPsiImplUtil.processOptional(localFuncDef.params) { signature, mask ->
                        val elementBuilder = LocalFunctionLookupElement(name, signature, localFuncDef)
                        elementBuilder.setHandler(FuncInsertHandler(localFuncDef).withMask(mask))
                        completionResultSet.addElement(elementBuilder)
                    }
                }
                true
            }
        }

        //global functions
        val project = cur.project
        if (has(GLOBAL_FUN)) {
            val context = SearchContext(project)
            val names = mutableListOf<String>()
            LuaGlobalFuncIndex.getInstance().processAllKeys(project) { name ->
                if (completionResultSet.prefixMatcher.prefixMatches(name)) {
                    names.add(name)
                }
                true
            }
            names.forEach { name ->
                val globalFuncDef = LuaGlobalFuncIndex.find(name, context)
                if (globalFuncDef != null) {
                    session.addWord(name)

                    LuaPsiImplUtil.processOptional(globalFuncDef.params) { signature, mask ->
                        val elementBuilder = GlobalFunctionLookupElement(name, signature, globalFuncDef)
                        elementBuilder.setHandler(GlobalFuncInsertHandler(name, project).withMask(mask))
                        completionResultSet.addElement(elementBuilder)
                    }
                }
            }
        }
        //global fields
        if (has(GLOBAL_VAR)) {
            val context = SearchContext(project)
            val names = mutableListOf<String>()
            LuaGlobalVarIndex.getInstance().processAllKeys(project) { name ->
                if (completionResultSet.prefixMatcher.prefixMatches(name)) {
                    names.add(name)
                }
                true
            }
            names.forEach { name ->
                val globalVar = LuaGlobalVarIndex.find(name, context)
                if (globalVar != null) {
                    session.addWord(name)
                    val elementBuilder = LuaTypeGuessableLookupElement(name, globalVar, false, LuaIcons.GLOBAL_FIELD)
                    completionResultSet.addElement(elementBuilder)
                }
            }
        }
        //key words
        if (has(KEY_WORDS)) {
            val keywords = TokenSet.orSet(KEYWORD_TOKENS, LuaSyntaxHighlighter.PRIMITIVE_TYPE_SET)
            for (keyWordToken in keywords.types) {
                session.addWord(keyWordToken.toString())

                completionResultSet.addElement(LookupElementBuilder.create(keyWordToken)
                        .withInsertHandler(KeywordInsertHandler(keyWordToken))
                )
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