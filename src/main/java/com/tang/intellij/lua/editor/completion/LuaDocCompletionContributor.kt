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
import com.intellij.lang.Language
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.*
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.lang.LuaParserDefinition
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassIndex
import com.tang.intellij.lua.ty.ITyClass

/**
 * doc 相关代码完成
 * Created by tangzx on 2016/12/2.
 */
class LuaDocCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, SHOW_DOC_TAG, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                val set = LuaParserDefinition.DOC_TAG_TOKENS
                for (type in set.types) {
                    completionResultSet.addElement(LookupElementBuilder.create(type).withIcon(LuaIcons.ANNOTATION))
                }
                ADDITIONAL_TAGS.forEach { tagName ->
                    completionResultSet.addElement(LookupElementBuilder.create(tagName).withIcon(LuaIcons.ANNOTATION))
                }
                completionResultSet.stopHere()
            }
        })

        extend(CompletionType.BASIC, SHOW_OPTIONAL, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                completionResultSet.addElement(LookupElementBuilder.create("optional"))
            }
        })

        extend(CompletionType.BASIC, AFTER_PARAM, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                var element = completionParameters.originalFile.findElementAt(completionParameters.offset - 1)
                if (element != null && element !is LuaDocPsiElement)
                    element = element.parent

                if (element is LuaDocPsiElement) {
                    val owner = LuaCommentUtil.findOwner(element)
                    if (owner is LuaFuncBodyOwner) {
                        val body = owner.funcBody
                        if (body != null) {
                            val parDefList = body.paramNameDefList
                            for (def in parDefList) {
                                completionResultSet.addElement(
                                        LookupElementBuilder.create(def.text)
                                                .withIcon(LuaIcons.PARAMETER)
                                )
                            }
                        }
                    }
                }
            }
        })

        extend(CompletionType.BASIC, SHOW_CLASS, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                val project = completionParameters.position.project
                LuaClassIndex.processKeys(project, Processor{
                    completionResultSet.addElement(LookupElementBuilder.create(it).withIcon(LuaIcons.CLASS))
                    true
                })
                completionResultSet.stopHere()
            }
        })

        extend(CompletionType.BASIC, SHOW_ACCESS_MODIFIER, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                completionResultSet.addElement(LookupElementBuilder.create("protected"))
                completionResultSet.addElement(LookupElementBuilder.create("public"))
            }
        })

        // 属性提示
        extend(CompletionType.BASIC, SHOW_FIELD, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                val position = completionParameters.position
                val comment = PsiTreeUtil.getParentOfType(position, LuaComment::class.java)
                val classDef = PsiTreeUtil.findChildOfType(comment, LuaDocTagClass::class.java)
                if (classDef != null) {
                    val classType = classDef.type
                    classType.processMembers(SearchContext(classDef.project)) { _, member ->
                        if (member is LuaClassField)
                            completionResultSet.addElement(LookupElementBuilder.create(member.name!!).withIcon(LuaIcons.CLASS_FIELD))
                        Unit
                    }
                }
            }
        })

        // @see member completion
        extend(CompletionType.BASIC, SHOW_SEE_MEMBER, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                val position = completionParameters.position
                val seeRefTag = PsiTreeUtil.getParentOfType(position, LuaDocTagSee::class.java)
                if (seeRefTag != null) {
                    val classType = seeRefTag.classNameRef?.resolveType() as? ITyClass
                    classType?.processMembers(SearchContext(seeRefTag.project)) { _, member ->
                        completionResultSet.addElement(LookupElementBuilder.create(member.name!!).withIcon(LuaIcons.CLASS_FIELD))
                        Unit
                    }
                }
                completionResultSet.stopHere()
            }
        })

        extend(CompletionType.BASIC, SHOW_LAN, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(completionParameters: CompletionParameters, processingContext: ProcessingContext, completionResultSet: CompletionResultSet) {
                Language.getRegisteredLanguages().forEach {
                    val fileType = it.associatedFileType
                    var lookupElement = LookupElementBuilder.create(it.id)
                    if (fileType != null)
                        lookupElement = lookupElement.withIcon(fileType.icon)
                    completionResultSet.addElement(lookupElement)
                }
                completionResultSet.stopHere()
            }
        })
    }

    companion object {

        // 在 @ 之后提示 param class type ...
        private val SHOW_DOC_TAG = psiElement(LuaDocTypes.TAG_NAME)

        // 在 @param 之后提示方法的参数
        private val AFTER_PARAM = psiElement().withParent(LuaDocParamNameRef::class.java)

        // 在 @param 之后提示 optional
        private val SHOW_OPTIONAL = psiElement().afterLeaf(
                psiElement(LuaDocTypes.TAG_NAME_PARAM))

        // 在 extends 之后提示类型
        private val SHOW_CLASS = psiElement().withParent(LuaDocClassNameRef::class.java)

        // 在 @field 之后提示 public / protected
        private val SHOW_ACCESS_MODIFIER = psiElement().afterLeaf(
                psiElement().withElementType(LuaDocTypes.TAG_NAME_FIELD)
        )

        private val SHOW_FIELD = psiElement(LuaDocTypes.ID).inside(LuaDocTagField::class.java)

        //@see type#MEMBER
        private val SHOW_SEE_MEMBER = psiElement(LuaDocTypes.ID).inside(LuaDocTagSee::class.java)

        private val SHOW_LAN = psiElement(LuaDocTypes.ID).inside(LuaDocTagLan::class.java)

        private val ADDITIONAL_TAGS = arrayOf("deprecated", "author", "version", "since")
    }
}
