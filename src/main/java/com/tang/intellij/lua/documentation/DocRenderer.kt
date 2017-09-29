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
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.psi.*
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.ty.*

internal inline fun StringBuilder.wrap(prefix: String, postfix: String, crossinline body: () -> Unit) {
    this.append(prefix)
    body()
    this.append(postfix)
}

internal inline fun StringBuilder.wrapTag(tag: String, crossinline body: () -> Unit) {
    wrap("<$tag>", "</$tag>", body)
}

internal fun StringBuilder.appendClassLink(clazz: String) {
    DocumentationManagerUtil.createHyperlink(this, clazz, clazz, true)
}

internal fun renderTy(sb: StringBuilder, ty: ITy) {
    when (ty) {
        is ITyClass -> {
            sb.appendClassLink(ty.className)
        }
        is ITyFunction -> {
            sb.append("fun")
            renderSignature(sb, ty.mainSignature)
        }
        is ITyArray -> {
            renderTy(sb, ty.base)
            sb.append("[]")
        }
        is TyUnknown -> {
            sb.appendClassLink("any")
        }
        is TyUnion -> {
            var idx = 0
            TyUnion.each(ty) {
                if (!it.isAnonymous) {
                    if (idx++ != 0) sb.append("|")
                    renderTy(sb, it)
                }
            }
        }
        is TyPrimitive -> {
            sb.append(ty.displayName)
        }
        else -> {
            sb.append("<<${ty.createTypeString()}>>")
        }
    }
}

internal fun renderSignature(sb: StringBuilder, sig: IFunSignature) {
    sb.wrap("(", "):") {
        var idx = 0
        sig.params.forEach {
            if (idx++ != 0) sb.append(", ")
            sb.append("${it.name}:")
            renderTy(sb, it.ty)
        }
    }
    renderTy(sb, sig.returnTy)
}

internal fun renderComment(sb: StringBuilder, comment: LuaComment?) {
    if (comment != null) {
        var child: PsiElement? = comment.firstChild
        while (child != null) {
            when (child) {
                is LuaDocParamDef -> {
                    renderDocParam(sb, child)
                    sb.append("<br>")
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
                        sb.append(markdownToHtml(child.text))
                    }
                }
            }
            child = child.nextSibling
        }
    }
}

internal fun renderClassDef(sb: StringBuilder, def: LuaDocClassDef) {
    val cls = def.type
    sb.append("class ")
    sb.wrapTag("b") { sb.appendClassLink(cls.className) }
    val superClassName = cls.superClassName
    if (superClassName != null) {
        sb.append(" : ")
        sb.appendClassLink(superClassName)
    }
    renderCommentString("  ", null, sb, def.commentString)
    sb.append("<br>")
}

internal fun renderFieldDef(sb: StringBuilder, def: LuaDocFieldDef) {
    val classDef = PsiTreeUtil.getChildOfType(def.parent, LuaDocClassDef::class.java)
    val cls = classDef?.type
    if (cls != null) {
        DocumentationManagerUtil.createHyperlink(sb, cls.className, cls.className, true)
        sb.append(".${def.name}")
        renderTypeSet(":", null, sb, def.typeSet)
        renderCommentString("  ", null, sb, def.commentString)
    }
}

internal fun renderDocParam(sb: StringBuilder, child: LuaDocParamDef) {
    val paramNameRef = child.paramNameRef
    if (paramNameRef != null) {
        sb.append("<li><b>param</b> ")
        sb.append(paramNameRef.text)
        renderTypeSet(":", null, sb, child.typeSet)
        renderCommentString("  ", null, sb, child.commentString)
    }
}

internal fun renderCommentString(prefix: String?, postfix: String?, sb: StringBuilder, child: LuaDocCommentString?) {
    child?.string?.text?.let {
        if (prefix != null) sb.append(prefix)
        sb.append(markdownToHtml(it))
        if (postfix != null) sb.append(postfix)
    }
}

internal fun renderTypeSet(prefix: String?, postfix: String?, sb: StringBuilder, typeSet: LuaDocTypeSet?) {
    if (typeSet != null) {
        if (prefix != null) sb.append(prefix)

        val nameRefList = typeSet.tyList
        for (i in nameRefList.indices) {
            if (i != 0) sb.append(", ")
            val docTy = nameRefList[i]
            renderTy(sb, docTy.getType())
        }

        if (postfix != null) sb.append(postfix)
    }
}

internal fun renderOverload(sb: StringBuilder, overloadDef: LuaDocOverloadDef) {
    overloadDef.functionTy?.getType()?.let {
        sb.append("<li><b>overload</b> ")
        renderTy(sb, it)
    }
}

internal fun renderTypeDef(sb: StringBuilder, typeDef: LuaDocTypeDef) {
    renderTy(sb, typeDef.type)
}