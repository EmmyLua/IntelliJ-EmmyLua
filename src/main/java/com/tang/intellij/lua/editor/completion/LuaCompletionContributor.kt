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
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.HashSet
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*

/**

 * Created by tangzx on 2016/11/27.
 */
class LuaCompletionContributor : CompletionContributor() {
    private var suggestWords = true
    init {
        //可以override
        extend(CompletionType.BASIC, SHOW_OVERRIDE, OverrideCompletionProvider())

        extend(CompletionType.BASIC, IN_CLASS_METHOD, SuggestSelfMemberProvider())

        //提示属性, 提示方法
        extend(CompletionType.BASIC, SHOW_CLASS_FIELD, ClassMemberCompletionProvider())

        extend(CompletionType.BASIC, SHOW_PATH, RequirePathCompletionProvider())

        //提示全局函数,local变量,local函数
        extend(CompletionType.BASIC, IN_NAME_EXPR, LocalAndGlobalCompletionProvider(LocalAndGlobalCompletionProvider.ALL))

        //_G.xxx
        //extend(CompletionType.BASIC, AFTER_G, LocalAndGlobalCompletionProvider(LocalAndGlobalCompletionProvider.GLOBALS))

        extend(CompletionType.BASIC, IN_CLASS_METHOD_NAME, LocalAndGlobalCompletionProvider(LocalAndGlobalCompletionProvider.VARS))
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val session = CompletionSession(parameters, result)
        parameters.editor.putUserData(CompletionSession.KEY, session)
        super.fillCompletionVariants(parameters, result)
        if (suggestWords && session.isSuggestWords && !result.isStopped) {
            suggestWordsInFile(parameters)
        }
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        suggestWords = true
        val file = context.file
        if (file is LuaFile) {
            val element = file.findElementAt(context.caret.offset - 1)
            if (element != null) {
                val type = element.node.elementType
                when (type) {
                    in IGNORE_SET -> {
                        suggestWords = false
                        context.dummyIdentifier = ""
                    }
                }
            }
        }
    }

    companion object {
        private val IGNORE_SET = TokenSet.create(LuaTypes.STRING, LuaTypes.NUMBER, LuaTypes.CONCAT)

        private val SHOW_CLASS_FIELD = psiElement(LuaTypes.ID)
                .withParent(LuaIndexExpr::class.java)

        private val IN_FUNC_NAME = psiElement(LuaTypes.ID)
                .withParent(LuaIndexExpr::class.java)
                .inside(LuaClassMethodName::class.java)
        private val AFTER_FUNCTION = psiElement()
                .afterLeaf(psiElement(LuaTypes.FUNCTION))
        private val IN_CLASS_METHOD_NAME = psiElement().andOr(IN_FUNC_NAME, AFTER_FUNCTION)

        private val IN_NAME_EXPR = psiElement(LuaTypes.ID)
                .withParent(LuaNameExpr::class.java)
        private val AFTER_G = psiElement(LuaTypes.ID)
                .afterLeafSkipping(psiElement(LuaTypes.DOT), psiElement().withText("_G"))

        private val SHOW_OVERRIDE = psiElement()
                .withParent(LuaClassMethodName::class.java)
        private val IN_CLASS_METHOD = psiElement(LuaTypes.ID)
                .withParent(LuaNameExpr::class.java)
                .inside(LuaClassMethodDef::class.java)
        private val SHOW_PATH = psiElement(LuaTypes.STRING)
                .inside(psiElement(LuaTypes.ARGS).afterLeaf("require"))

        private fun suggestWordsInFile(parameters: CompletionParameters) {
            val session = CompletionSession.get(parameters)!!

            val wordsInFileSet = HashSet<String>()
            val file = session.parameters.originalFile
            file.acceptChildren(object : LuaVisitor() {
                override fun visitPsiElement(o: LuaPsiElement) {
                    o.acceptChildren(this)
                }

                override fun visitElement(element: PsiElement) {
                    if (element.node.elementType === LuaTypes.ID && element.textLength > 2) {
                        val text = element.text
                        if (session.resultSet.prefixMatcher.prefixMatches(text) && session.addWord(text))
                            wordsInFileSet.add(text)
                    }
                    super.visitElement(element)
                }
            })

            for (s in wordsInFileSet) {
                session.resultSet.addElement(PrioritizedLookupElement.withPriority(LookupElementBuilder
                        .create(s)
                        .withIcon(LuaIcons.WORD), -1.0)//.withTypeText("Word In File")
                )
            }
        }
    }
}