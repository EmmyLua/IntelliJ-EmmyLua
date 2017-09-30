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

package com.tang.intellij.lua.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef
import com.tang.intellij.lua.editor.completion.LuaDocumentationLookupElement
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassIndex
import com.tang.intellij.lua.ty.TyFunction
import com.tang.intellij.lua.ty.isSelfCall

/**
 * Documentation support
 * Created by tangzx on 2016/12/10.
 */
class LuaDocumentationProvider : AbstractDocumentationProvider(), DocumentationProvider {

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element != null) {
            when (element) {
                is LuaTypeGuessable -> {
                    val ty = element.guessTypeFromCache(SearchContext(element.project))
                    return buildString {
                        renderTy(this, ty)
                    }
                }
            }
        }
        return super.getQuickNavigateInfo(element, originalElement)
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, obj: Any, element: PsiElement): PsiElement? {
        if (obj is LuaDocumentationLookupElement) {
            return obj.getDocumentationElement(SearchContext(psiManager.project))
        }
        return super.getDocumentationElementForLookupItem(psiManager, obj, element)
    }

    override fun getDocumentationElementForLink(psiManager: PsiManager, link: String, context: PsiElement?): PsiElement? {
        return LuaClassIndex.find(link, SearchContext(psiManager.project))
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        val sb = StringBuilder()
        when (element) {
            is LuaParamNameDef -> renderParamNameDef(sb, element)
            is LuaDocClassDef -> renderClassDef(sb, element)
            is LuaClassMember -> renderClassMember(sb, element)
            is LuaNameDef -> { //local xx
                sb.wrapTag("pre") {
                    sb.append("local ${element.name}")
                }

                val owner = PsiTreeUtil.getParentOfType(element, LuaCommentOwner::class.java)
                if (owner != null)
                    renderComment(sb, owner.comment)
            }
        }
        if (sb.isNotEmpty()) return sb.toString()
        return super.generateDoc(element, originalElement)
    }

    private fun renderClassMember(sb: StringBuilder, classMember: LuaClassMember) {
        val context = SearchContext(classMember.project)
        val parentType = classMember.guessClassType(context)
        val ty = classMember.guessType(context)

        //base info
        if (parentType != null) {
            renderTy(sb, parentType)
            with(sb) {
                when (ty) {
                    is TyFunction -> {
                        append(if (ty.isSelfCall) ":" else ".")
                        append(classMember.name)
                        renderSignature(sb, ty.mainSignature)
                    }
                    else -> {
                        append(".${classMember.name}:")
                        renderTy(sb, ty)
                    }
                }
            }
        } else {
            //NameExpr
            if (classMember is LuaNameExpr) {
                val nameExpr: LuaNameExpr = classMember
                with(sb) {
                    append(nameExpr.name)
                    when (ty) {
                        is TyFunction -> renderSignature(sb, ty.mainSignature)
                        else -> {
                            append(":")
                            renderTy(sb, ty)
                        }
                    }
                }

                val stat = nameExpr.parent.parent // VAR_LIST ASSIGN_STAT
                if (stat is LuaAssignStat) renderComment(sb, stat.comment)
            }
        }

        //comment content
        if (classMember is LuaCommentOwner)
            renderComment(sb, classMember.comment)
        else if (classMember is LuaDocFieldDef)
            renderCommentString("  ", null, sb, classMember.commentString)
    }

    private fun renderParamNameDef(sb: StringBuilder, paramNameDef: LuaParamNameDef) {
        val owner = PsiTreeUtil.getParentOfType(paramNameDef, LuaCommentOwner::class.java)
        val docParamDef = owner?.comment?.getParamDef(paramNameDef.name)
        if (docParamDef != null) {
            renderDocParam(sb, docParamDef)
        } else {
            sb.append("<li><b>param</b> ${paramNameDef.name}:any")
        }
    }
}
