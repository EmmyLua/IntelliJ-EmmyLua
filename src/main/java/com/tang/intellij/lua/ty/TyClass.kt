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

package com.tang.intellij.lua.ty

import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.LuaPredefinedScope
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex
import com.tang.intellij.lua.stubs.index.LuaClassIndex
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex


abstract class TyClass(val className: String, open val superClassName: String? = null) : Ty(TyKind.Class) {

    open var aliasName: String? = null

    open fun processFields(context: SearchContext, processor: (TyClass, LuaClassField) -> Unit) {
        val clazzName = className
        val project = context.project

        val fieldIndex = LuaClassFieldIndex.getInstance()
        val list = fieldIndex.get(clazzName, project, LuaPredefinedScope(project))

        val alias = aliasName
        if (alias != null) {
            val classFields = fieldIndex.get(alias, project, LuaPredefinedScope(project))
            list.addAll(classFields)
        }

        for (field in list) {
            processor(this, field)
        }

        // super
        val superType = getSuperClass(context)
        superType?.processFields(context, processor)
    }
    open fun processMethods(context: SearchContext, processor: (TyClass, LuaClassMethod) -> Unit) {
        val clazzName = className
        val project = context.project

        val methodIndex = LuaClassMethodIndex.getInstance()
        val list = methodIndex.get(clazzName, project, LuaPredefinedScope(project))

        val alias = aliasName
        if (alias != null) {
            list.addAll(methodIndex.get(alias, project, LuaPredefinedScope(project)))
        }
        for (def in list) {
            val methodName = def.name
            if (methodName != null) {
                processor(this, def)
            }
        }

        val superType = getSuperClass(context)
        superType?.processMethods(context, processor)
    }

    fun processStaticMethods(context: SearchContext,
                             processor: (TyClass, LuaClassMethod) -> Unit) {
        val clazzName = className
        val list = LuaClassMethodIndex.findStaticMethods(clazzName, context)

        val alias = aliasName
        if (alias != null) {
            list.addAll(LuaClassMethodIndex.findStaticMethods(alias, context))
        }
        for (def in list) {
            val methodName = def.name
            if (methodName != null) {
                processor(this, def)
            }
        }

        val superType = getSuperClass(context)
        superType?.processStaticMethods(context, processor)
    }

    override val displayName: String get() = className

    open fun initAliasName(context: SearchContext) {
        if (aliasName == null) {
            val classDef = LuaClassIndex.find(className, context)
            if (classDef != null) {
                aliasName = classDef.classType.className
            }
        }
    }

    fun getSuperClass(context: SearchContext): TyClass? {
        val clsName = superClassName
        if (clsName != null) {
            val def = LuaClassIndex.find(clsName, context)
            return def?.classType
        }
        return null
    }

    fun findMethod(name: String, searchContext: SearchContext): LuaClassMethod? {
        val className = className
        var def = LuaClassMethodIndex.findMethodWithName(className, name, searchContext)
        if (def == null) { // static
            def = LuaClassMethodIndex.findStaticMethod(className, name, searchContext)
        }
        if (def == null) { // super
            val superType = getSuperClass(searchContext)
            if (superType != null)
                def = superType.findMethod(name, searchContext)
        }
        return def
    }

    fun findField(name: String, searchContext: SearchContext): LuaClassField? {
        var def = LuaClassFieldIndex.find(this, name, searchContext)
        if (def == null) {
            val superType = getSuperClass(searchContext)
            if (superType != null)
                def = superType.findField(name, searchContext)
        }
        return def
    }

    companion object {
        // for _G
        val G:TyClass = TySerializedClass(Constants.WORD_G)

        fun createAnonymousType(nameDef: LuaNameDef): TyClass {
            return TySerializedClass(nameDef.name)
        }

        fun createGlobalType(nameExpr: LuaNameExpr): TyClass {
            return TySerializedClass(nameExpr.text)
        }
    }
}

class TyPsiDocClass(val classDef: LuaDocClassDef) : TyClass(classDef.name) {
    private val _supperName: String? by lazy {
        val supperRef = classDef.superClassNameRef
        if (supperRef != null)
            return@lazy supperRef.text
        else null
    }
    override val superClassName: String?
        get() = _supperName

    override fun initAliasName(context: SearchContext) {
        aliasName = classDef.aliasName
    }
}

class TySerializedClass(name: String, supper: String? = null, override var aliasName: String? = null)
    : TyClass(name, supper)

fun getTableTypeName(table: LuaTableExpr): String {
    val fileName = table.containingFile.name
    return "$fileName@(${table.node.startOffset})table"
}

class TyTable(val table: LuaTableExpr) : TyClass(getTableTypeName(table)) {
    override fun processFields(context: SearchContext, processor: (TyClass, LuaClassField) -> Unit) {
        for (field in table.tableFieldList) {
            processor(this, field)
        }
        super.processFields(context, processor)
    }

    override val displayName: String
        get() = "table"

    override fun initAliasName(context: SearchContext) = Unit
}