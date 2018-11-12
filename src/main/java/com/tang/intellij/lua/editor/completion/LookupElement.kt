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

@file:Suppress("EqualsOrHashCode")

package com.tang.intellij.lua.editor.completion

import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaPsiElement
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*
import javax.swing.Icon

interface LuaDocumentationLookupElement {
    fun getDocumentationElement(context: SearchContext): PsiElement?
}

/**
 * lookup elements
 * Created by TangZX on 2017/5/22.
 */

open class LuaTypeGuessableLookupElement(name: String, val psi: LuaPsiElement, private val type: ITy, bold: Boolean, icon: Icon)
    : LuaLookupElement(name, bold, icon) {
    private var typeString: String? = null

    init {
        lookupString = name
    }

    override fun getTypeText(): String? {
        if (typeString == null) {
            typeString = type.displayName
            if (typeString == null) {
                typeString = Constants.WORD_ANY
            }
        }
        return typeString
    }

    /**
     * https://github.com/tangzx/IntelliJ-EmmyLua/issues/54
     * @see [com.tang.intellij.lua.documentation.LuaDocumentationProvider]
     */
    override fun getObject(): Any {
        return psi
    }

    override fun equals(other: Any?): Boolean {
        return other is LuaTypeGuessableLookupElement && super.equals(other)
    }
}

class LuaFieldLookupElement(val fieldName: String, val field: LuaClassField, val ty:ITy?, bold: Boolean)
    : LuaLookupElement(fieldName, bold, null), LuaDocumentationLookupElement {

    override fun getDocumentationElement(context: SearchContext): PsiElement? {
        if (field.isValid)
            return field
        else {
            val clazz = TyUnion.getPerfectClass(type)
            if (clazz != null) {
                return clazz.findMember(fieldName, context)
            }
        }
        return null
    }

    val type: ITy by lazy {
        ty ?: field.guessType(SearchContext(field.project))
    }

    private fun lazyInit() {
        icon = field.visibility.warpIcon(LuaIcons.CLASS_FIELD)
        typeText = type.displayName
    }

    override fun renderElement(presentation: LookupElementPresentation?) {
        if (icon == null)
            lazyInit()
        super.renderElement(presentation)
    }
}

class TyFunctionLookupElement(name: String,
                              val psi: LuaPsiElement,
                              private val signature: IFunSignature,
                              bold: Boolean,
                              colonStyle: Boolean,
                              val ty: ITyFunction,
                              icon: Icon
) : LuaLookupElement(name, bold, icon) {
    /*init {
        val list = mutableListOf<String>()
        signature.processArgs(null, colonStyle) { _, param ->
            list.add(param.name)
            true
        }
        itemText = lookupString + "(${list.joinToString(", ")})"
        typeText = signature.returnTy.displayName
    }*/

    private val lazyTypeText by lazy { signature.returnTy.displayName }

    override fun getTypeText() = lazyTypeText

    private val lazyItemText by lazy {
        val list = mutableListOf<String>()
        signature.processArgs(null, colonStyle) { _, param ->
            list.add(param.name)
            true
        }
        if (signature.hasVarargs())
            list.add("...")
        "$lookupString(${list.joinToString(", ")})"
    }

    override fun getItemText() = myItemString ?: lazyItemText

    override fun hashCode(): Int {
        return super.hashCode() * 31 * (signature.params.size + 1)
    }

    /**
     * https://github.com/tangzx/IntelliJ-EmmyLua/issues/54
     */
    override fun getObject(): Any {
        return psi
    }
}