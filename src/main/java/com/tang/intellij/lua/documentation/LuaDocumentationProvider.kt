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

import com.intellij.codeInsight.documentation.DocumentationManagerUtil
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.comment.psi.LuaDocTagField
import com.tang.intellij.lua.editor.completion.LuaDocumentationLookupElement
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassIndex
import com.tang.intellij.lua.ty.*

/**
 * Documentation support
 * Created by tangzx on 2016/12/10.
 */
class LuaDocumentationProvider : AbstractDocumentationProvider(), DocumentationProvider {

    private val renderer: ITyRenderer = object: TyRenderer() {
        override fun renderType(t: String): String {
            return if (t.isNotEmpty()) buildString { DocumentationManagerUtil.createHyperlink(this, t, t, true) } else t
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element != null) {
            when (element) {
                is LuaTypeGuessable -> {
                    val ty = element.guessType(SearchContext(element.project))
                    return buildString {
                        renderTy(this, ty, renderer)
                    }
                }
            }
        }
        return super.getQuickNavigateInfo(element, originalElement)
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, obj: Any, element: PsiElement?): PsiElement? {
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
        val tyRenderer = renderer
        when (element) {
            is LuaParamNameDef -> renderParamNameDef(sb, element)
            is LuaDocTagClass -> renderClassDef(sb, element, tyRenderer)
            is LuaClassMember -> renderClassMember(sb, element)
            is LuaNameDef -> { //local xx

                renderDefinition(sb) {
                    sb.append("local <b>${element.name}</b>:")
                    val ty = element.guessType(SearchContext(element.project))
                    renderTy(sb, ty, tyRenderer)
                }

                val owner = PsiTreeUtil.getParentOfType(element, LuaCommentOwner::class.java)
                owner?.let { renderComment(sb, owner.comment, tyRenderer) }
            }
            is LuaLocalFuncDef -> {
                sb.wrapTag("pre") {
                    sb.append("local function <b>${element.name}</b>")
                    val type = element.guessType(SearchContext(element.project)) as ITyFunction
                    renderSignature(sb, type.mainSignature, tyRenderer)
                }
                renderComment(sb, element.comment, tyRenderer)
            }
        }
        if (sb.isNotEmpty()) return sb.toString()
        return super.generateDoc(element, originalElement)
    }

    private fun renderClassMember(sb: StringBuilder, classMember: LuaClassMember) {
        val context = SearchContext(classMember.project)
        val parentType = classMember.guessClassType(context)
        val ty = classMember.guessType(context)
        val tyRenderer = renderer

        renderDefinition(sb) {
            //base info
            if (parentType != null) {
                renderTy(sb, parentType, tyRenderer)
                with(sb) {
                    when (ty) {
                        is TyFunction -> {
                            append(if (ty.isColonCall) ":" else ".")
                            append(classMember.name)
                            renderSignature(sb, ty.mainSignature, tyRenderer)
                        }
                        else -> {
                            append(".${classMember.name}:")
                            renderTy(sb, ty, tyRenderer)
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
                            is TyFunction -> renderSignature(sb, ty.mainSignature, tyRenderer)
                            else -> {
                                append(":")
                                renderTy(sb, ty, tyRenderer)
                            }
                        }
                    }

                    val stat = nameExpr.parent.parent // VAR_LIST ASSIGN_STAT
                    if (stat is LuaAssignStat) renderComment(sb, stat.comment, tyRenderer)
                }
            }
        }

        //comment content
        when (classMember) {
            is LuaCommentOwner -> renderComment(sb, classMember.comment, tyRenderer)
            is LuaDocTagField -> renderCommentString("  ", null, sb, classMember.commentString)
            is LuaIndexExpr -> {
                val p1 = classMember.parent
                val p2 = p1.parent
                if (p1 is LuaVarList && p2 is LuaAssignStat) {
                    renderComment(sb, p2.comment, tyRenderer)
                }
            }
        }
    }

    private fun renderParamNameDef(sb: StringBuilder, paramNameDef: LuaParamNameDef) {
        val owner = PsiTreeUtil.getParentOfType(paramNameDef, LuaCommentOwner::class.java)
        val docParamDef = owner?.comment?.getParamDef(paramNameDef.name)
        val tyRenderer = renderer
        if (docParamDef != null) {
            renderDocParam(sb, docParamDef, tyRenderer, true)
        } else {
            val ty = infer(paramNameDef, SearchContext(paramNameDef.project))
            sb.append("<b>param</b> <code>${paramNameDef.name}</code> : ")
            renderTy(sb, ty, tyRenderer)
        }
    }
}
