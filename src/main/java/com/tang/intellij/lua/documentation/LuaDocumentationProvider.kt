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
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.*
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.psi.LuaCommentOwner
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import com.tang.intellij.lua.psi.LuaNameDef
import com.tang.intellij.lua.psi.LuaParamNameDef
import java.util.*

/**
 * Documentation support
 * Created by tangzx on 2016/12/10.
 */
class LuaDocumentationProvider : AbstractDocumentationProvider(), DocumentationProvider {

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        var doc = genDoc(element)
        if (doc == null)
            doc = super.generateDoc(element, originalElement)
        return doc
    }

    private fun genDoc(element: PsiElement): String? {
        when (element) {
            is LuaCommentOwner -> return genDoc(element)
            is LuaParamNameDef -> return genDoc(element)
            is LuaNameDef -> {
                val owner = PsiTreeUtil.getParentOfType(element, LuaCommentOwner::class.java)
                if (owner != null)
                    return genDoc(owner)
            }
        }
        return null
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
            val funcBodyOwner = owner as LuaFuncBodyOwner
            val methodDef = owner as PsiNameIdentifierOwner
            sb.append("<h1>")
            sb.append(methodDef.name)
            sb.append(funcBodyOwner.paramSignature)
            sb.append("</h1><br>")
        }

        val comment = LuaCommentUtil.findComment(owner)
        sb.append(genComment(comment))

        val doc = sb.toString()
        if (doc.isNotEmpty())
            return sb.toString()
        else
            return null
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
                            sb.append(" ")
                            getTypeSet(child.typeSet, sb)
                            sb.append(" " + child.commentString?.string?.text)
                            sb.append("<br>")
                        }
                    }
                    is LuaDocReturnDef -> {
                        val typeList = child.typeList
                        if (typeList != null) {
                            sb.append("<li><b>return</b> ")
                            val typeSetList = typeList.typeSetList
                            for (typeSet in typeSetList) {
                                getTypeSet(typeSet, sb)
                                sb.append(" ")
                            }
                            sb.append("<br>")
                        }
                    }
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

    private fun getTypeSet(typeSet: LuaDocTypeSet?, sb: StringBuilder) {
        if (typeSet != null) {
            sb.append("(")
            val nameRefList = typeSet.classNameRefList
            val names = arrayOfNulls<String>(nameRefList.size)
            for (i in nameRefList.indices) {
                names[i] = nameRefList[i].text
            }
            sb.append(names.joinToString(", "))
            sb.append(")")
        }
    }
}
