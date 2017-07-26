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
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import javax.swing.Icon

/**
 * lookup elements
 * Created by TangZX on 2017/5/22.
 */

open class LuaTypeGuessableLookupElement(name: String, private val guessable: LuaTypeGuessable, bold: Boolean, icon: Icon)
    : LuaLookupElement(name, bold, icon) {
    private var typeString: String? = null

    init {
        lookupString = name
    }

    override fun getTypeText(): String? {
        if (typeString == null) {
            val set = guessable.guessType(SearchContext(guessable.project))
            typeString = set?.createTypeString()
            if (typeString == null) {
                typeString = "any"
            }
        }
        return typeString
    }

    override fun equals(other: Any?): Boolean {
        return other is LuaTypeGuessableLookupElement && super.equals(other)
    }
}

class LuaFieldLookupElement(fieldName: String, field: LuaClassField, bold: Boolean)
    : LuaTypeGuessableLookupElement(fieldName, field, bold, LuaIcons.CLASS_FIELD)

abstract class LuaFunctionLookupElement(name:String, signature: String, bold:Boolean, val bodyOwner: LuaFuncBodyOwner, icon: Icon)
    : LuaLookupElement(name, bold, icon) {
    init {
        itemText = lookupString + signature
    }

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

    override fun getObject(): Any {
        return bodyOwner
    }
}

class LuaMethodLookupElement(name:String, signature: String, bold:Boolean, method: LuaClassMethodDef)
    : LuaFunctionLookupElement(name, signature, bold, method, LuaIcons.CLASS_METHOD)

class LocalFunctionLookupElement(name:String, signature: String, method: LuaLocalFuncDef)
    : LuaFunctionLookupElement(name, signature, false, method, LuaIcons.LOCAL_FUNCTION)

class GlobalFunctionLookupElement(name:String, signature: String, method: LuaGlobalFuncDef)
    : LuaFunctionLookupElement(name, signature, false, method, LuaIcons.GLOBAL_FUNCTION)