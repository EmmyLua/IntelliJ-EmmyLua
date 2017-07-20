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
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.util.containers.HashSet
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.*

import com.intellij.patterns.PlatformPatterns.psiElement

/**

 * Created by tangzx on 2016/11/27.
 */
class LuaCompletionContributor : CompletionContributor() {
    init {
        //可以override
        extend(CompletionType.BASIC, SHOW_OVERRIDE, OverrideCompletionProvider())

        extend(CompletionType.BASIC, IN_CLASS_METHOD, SuggestSelfMemberProvider())

        //提示属性, 提示方法
        extend(CompletionType.BASIC, SHOW_CLASS_FIELD, ClassMemberCompletionProvider())

        extend(CompletionType.BASIC, SHOW_PATH, RequirePathCompletionProvider())

        //提示全局函数,local变量,local函数
        extend(CompletionType.BASIC, psiElement().inside(LuaFile::class.java)
                .andNot(SHOW_CLASS_FIELD)
                .andNot(IN_LITERAL)
                .andNot(IN_COMMENT)
                .andNot(IN_CLASS_METHOD_NAME)
                .andNot(IN_PARAM_NAME)
                .andNot(SHOW_OVERRIDE), LocalAndGlobalCompletionProvider(LocalAndGlobalCompletionProvider.ALL))

        extend(CompletionType.BASIC, IN_CLASS_METHOD_NAME, LocalAndGlobalCompletionProvider(LocalAndGlobalCompletionProvider.VARS))
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val session = CompletionSession(parameters, result)
        parameters.editor.putUserData(CompletionSession.KEY, session)
        super.fillCompletionVariants(parameters, result)
        if (session.isSuggestWords && !result.isStopped) {
            suggestWordsInFile(parameters)
        }
    }

    companion object {

        private val SHOW_CLASS_FIELD = psiElement(LuaTypes.ID)
                .withParent(LuaIndexExpr::class.java)
        private val IN_PARAM_NAME = psiElement(LuaTypes.ID)
                .withParent(LuaParamNameDef::class.java)


        private val IN_FUNC_NAME = psiElement(LuaTypes.ID)
                .withParent(LuaIndexExpr::class.java)
                .inside(LuaClassMethodName::class.java)
        private val AFTER_FUNCTION = psiElement()
                .afterLeaf(psiElement(LuaTypes.FUNCTION))
        private val IN_CLASS_METHOD_NAME = psiElement().andOr(IN_FUNC_NAME, AFTER_FUNCTION)


        private val IN_COMMENT = psiElement()
                .inside(PsiComment::class.java)
        private val SHOW_OVERRIDE = psiElement()
                .withParent(LuaClassMethodName::class.java)
        private val IN_CLASS_METHOD = psiElement(LuaTypes.ID)
                .withParent(LuaNameExpr::class.java)
                .inside(LuaClassMethodDef::class.java)
        private val SHOW_PATH = psiElement(LuaTypes.STRING)
                .inside(psiElement(LuaTypes.ARGS).afterLeaf("require"))
        private val IN_LITERAL = psiElement().inside(LuaLiteralExpr::class.java)

        private fun suggestWordsInFile(parameters: CompletionParameters) {
            val session = CompletionSession.get(parameters)!!

            val wordsInFileSet = HashSet<String>()
            val file = session.parameters.originalFile
            file.acceptChildren(object : LuaVisitor() {
                override fun visitPsiElement(o: LuaPsiElement) {
                    o.acceptChildren(this)
                }

                override fun visitElement(element: PsiElement?) {
                    if (element!!.node.elementType === LuaTypes.ID && element!!.textLength > 2) {
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