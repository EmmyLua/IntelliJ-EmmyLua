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

import com.tang.intellij.lua.lang.LuaIcons
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaClassMethodDef
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import com.tang.intellij.lua.search.SearchContext
import javax.swing.Icon

/**
 * lookup elements
 * Created by TangZX on 2017/5/22.
 */

class LuaFieldLookupElement internal constructor(private val fieldName: String, private val field: LuaClassField, bold: Boolean)
    : LuaLookupElement(fieldName, bold, LuaIcons.CLASS_FIELD) {
    private var typeString: String? = null

    override fun getLookupString(): String {
        return fieldName
    }

    override fun getTypeText(): String? {
        if (typeString == null) {
            val set = field.guessType(SearchContext(field.project))
            typeString = set.createTypeString()
        }
        return typeString
    }

    override fun equals(other: Any?): Boolean {
        return other is LuaFieldLookupElement && super.equals(other)
    }
}

abstract class LuaFunctionLookupElement(name:String, val signature: String, bold:Boolean, val bodyOwner: LuaFuncBodyOwner, icon: Icon)
    : LuaLookupElement(name, bold, icon) {

    var typeDesc: String? = null

    override fun equals(other: Any?): Boolean {
        return other is LuaFunctionLookupElement && super.equals(other)
    }

    override fun getTypeText(): String? {
        if (typeDesc == null) {
            val set = bodyOwner.guessReturnTypeSet(SearchContext(bodyOwner.project))
            typeDesc = set.createReturnString()
        }
        return typeDesc
    }

    override fun getItemText(): String {
        return lookupString + signature
    }
}

class LuaMethodLookupElement(name:String, signature: String, bold:Boolean, method: LuaClassMethodDef)
    : LuaFunctionLookupElement(name, signature, bold, method, LuaIcons.CLASS_METHOD)