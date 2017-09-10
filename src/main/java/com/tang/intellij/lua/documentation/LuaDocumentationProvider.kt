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
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.*
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassIndex
import com.tang.intellij.lua.ty.TyUnion
import java.util.*

/**
 * Documentation support
 * Created by tangzx on 2016/12/10.
 */
class LuaDocumentationProvider : AbstractDocumentationProvider(), DocumentationProvider {

    private inline fun StringBuilder.wrap(prefix: String, postfix: String, crossinline body: () -> Unit) {
        this.append(prefix)
        body()
        this.append(postfix)
    }

    private inline fun StringBuilder.wrapTag(tag: String, crossinline body: () -> Unit) {
        wrap("<$tag>", "</$tag>", body)
    }

    private fun StringBuilder.appendClassLink(clazz: String) {
        DocumentationManagerUtil.createHyperlink(this, clazz, clazz, true)
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element != null) {
            when (element) {
                is LuaTypeGuessable -> {
                    val ty = element.guessTypeFromCache(SearchContext(element.project))
                    return buildString {
                        TyUnion.each(ty) {
                            wrapTag("br") { append(it.toString()) }
                        }
                    }
                }
            }
        }
        return super.getQuickNavigateInfo(element, originalElement)
    }

    override fun getDocumentationElementForLink(psiManager: PsiManager, link: String, context: PsiElement?): PsiElement? {
        return LuaClassIndex.find(link, SearchContext(psiManager.project))
        //return super.getDocumentationElementForLink(psiManager, link, context)
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        when (element) {
            is LuaCommentOwner -> return genDoc(element)
            is LuaParamNameDef -> return genDoc(element)
            is LuaDocClassDef -> return buildString { renderClassDef(this, element) }
            is LuaDocFieldDef -> return buildString { renderFieldDef(this, element) }
            is LuaNameDef -> {
                val owner = PsiTreeUtil.getParentOfType(element, LuaCommentOwner::class.java)
                if (owner != null)
                    return genDoc(owner)
            }
        }
        return super.generateDoc(element, originalElement)
    }

    private fun genDoc(paramNameDef: LuaParamNameDef): String? {
        val owner = PsiTreeUtil.getParentOfType(paramNameDef, LuaCommentOwner::class.java)
        val o = Optional.ofNullable(owner)
                .map<LuaComment>({ it.comment })
                .map<LuaDocParamDef> { t -> t.getParamDef(paramNameDef.name) }
                .map<LuaDocCommentString>({ it.commentString })
                .map<PsiElement>({ it.string })
                .map<String>({ it.text })
        return o.orElse(null)
    }

    private fun genDoc(owner: LuaCommentOwner): String? {
        val sb = StringBuilder()
        if (owner is LuaFuncBodyOwner && owner is PsiNameIdentifierOwner) {
            val methodDef: PsiNameIdentifierOwner = owner
            with(sb) {
                append("<h1>")
                append(methodDef.name)
                append(owner.paramSignature)
                append("</h1><br>")
            }
        }

        val comment = LuaCommentUtil.findComment(owner)
        sb.append(genComment(comment))

        val doc = sb.toString()
        return if (doc.isNotEmpty()) sb.toString() else null
    }

    private fun genComment(comment: LuaComment?): String {
        val sb = StringBuilder()
        if (comment != null) {
            var child: PsiElement? = comment.firstChild
            while (child != null) {
                when (child) {
                    is LuaDocParamDef -> {
                        val paramNameRef = child.paramNameRef
                        if (paramNameRef != null) {
                            sb.append("<li><b>param</b> ")
                            sb.append(paramNameRef.text)
                            renderTypeSet(":", null, sb, child.typeSet)
                            renderCommentString("  ", null, sb, child.commentString)
                            sb.append("<br>")
                        }
                    }
                    is LuaDocReturnDef -> {
                        val typeList = child.typeList
                        if (typeList != null) {
                            sb.append("<li><b>return</b> ")
                            val typeSetList = typeList.typeSetList
                            for (typeSet in typeSetList) {
                                renderTypeSet(":", null, sb, typeSet)
                                sb.append(" ")
                            }
                            sb.append("<br>")
                        }
                    }
                    is LuaDocClassDef -> renderClassDef(sb, child)
                    is LuaDocOverloadDef -> renderOverload(sb, child)
                    is LuaDocTypeDef -> renderTypeDef(sb, child)
                    else -> {
                        val elementType = child.node.elementType
                        if (elementType === LuaDocTypes.STRING) {
                            sb.append(child.text)
                            sb.append("<br>")
                        }
                    }
                }
                child = child.nextSibling
            }
        }
        return sb.toString()
    }

    private fun renderClassDef(sb: StringBuilder, def: LuaDocClassDef) {
        val cls = def.type
        sb.append("class ")
        sb.wrapTag("b") { sb.append(cls.className) }
        if (cls.superClassName != null) {
            sb.append(" : ")
            DocumentationManagerUtil.createHyperlink(sb, cls.superClassName, cls.superClassName, true)
        }
        renderCommentString("  ", null, sb, def.commentString)
        sb.append("<br>")
    }

    private fun renderFieldDef(sb: StringBuilder, def: LuaDocFieldDef) {
        val classDef = PsiTreeUtil.getChildOfType(def.parent, LuaDocClassDef::class.java)
        val cls = classDef?.type
        if (cls != null) {
            DocumentationManagerUtil.createHyperlink(sb, cls.className, cls.className, true)
            sb.append(".${def.name}")
            renderTypeSet(":", null, sb, def.typeSet)
            renderCommentString("  ", null, sb, def.commentString)
        }
    }

    private fun renderCommentString(prefix: String?, postfix: String?, sb: StringBuilder, child: LuaDocCommentString?) {
        child?.string?.text?.let {
            if (prefix != null) sb.append(prefix)
            sb.append(it)
            if (postfix != null) sb.append(postfix)
        }
    }

    private fun renderTypeSet(prefix: String?, postfix: String?, sb: StringBuilder, typeSet: LuaDocTypeSet?) {
        if (typeSet != null) {
            if (prefix != null) sb.append(prefix)
            sb.append("(")
            val nameRefList = typeSet.tyList
            for (i in nameRefList.indices) {
                if (i != 0) sb.append(", ")
                sb.appendClassLink(nameRefList[i].text)
            }
            sb.append(")")
            if (postfix != null) sb.append(postfix)
        }
    }

    private fun renderOverload(sb: StringBuilder, overloadDef: LuaDocOverloadDef) {
        sb.append("<li><b>overload</b> ")
        sb.append(overloadDef.functionTy.toString())
    }

    private fun renderTypeDef(sb: StringBuilder, typeDef: LuaDocTypeDef) {
        sb.append(typeDef.type.createTypeString())
    }
}
