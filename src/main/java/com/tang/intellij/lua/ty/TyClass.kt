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
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex

interface ITyClass : ITy {
    val className: String
    var superClassName: String?
    var aliasName: String?
    fun lazyInit(searchContext: SearchContext)
    fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit)
    fun findMember(name: String, searchContext: SearchContext): LuaClassMember?
    fun processFields(context: SearchContext, processor: (ITyClass, LuaClassField) -> Unit)
    fun processMethods(context: SearchContext, processor: (ITyClass, LuaClassMethod) -> Unit)
    fun processStaticMethods(context: SearchContext, processor: (ITyClass, LuaClassMethod) -> Unit)
    fun findMethod(name: String, searchContext: SearchContext): LuaClassMethod?
    fun findField(name: String, searchContext: SearchContext): LuaClassField?
    fun getSuperClass(context: SearchContext): ITyClass?
}

abstract class TyClass(override val className: String, override var superClassName: String? = null) : Ty(TyKind.Class), ITyClass {

    final override var aliasName: String? = null

    private var _lazyInitialized: Boolean = false

    override fun equals(other: Any?): Boolean {
        return other is ITyClass && other.className == className
    }

    override fun hashCode(): Int {
        return className.hashCode()
    }

    override fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit) {
        val clazzName = className
        val project = context.project

        val memberIndex = LuaClassMemberIndex.instance
        val list = memberIndex.get(clazzName, project, LuaPredefinedScope(project))

        val alias = aliasName
        if (alias != null) {
            val classMembers = memberIndex.get(alias, project, LuaPredefinedScope(project))
            list.addAll(classMembers)
        }

        for (member in list) {
            processor(this, member)
        }

        // super
        val superType = getSuperClass(context)
        superType?.processMembers(context, processor)
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return LuaClassMemberIndex.find(this, name, searchContext)
    }

    override fun processFields(context: SearchContext, processor: (ITyClass, LuaClassField) -> Unit) {
        val clazzName = className
        val project = context.project

        val fieldIndex = LuaClassFieldIndex.instance
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

    override fun processMethods(context: SearchContext, processor: (ITyClass, LuaClassMethod) -> Unit) {
        val clazzName = className
        val project = context.project

        val methodIndex = LuaClassMethodIndex.instance
        var list = methodIndex.get(clazzName, project, LuaPredefinedScope(project))

        val alias = aliasName
        if (alias != null) {
            list += methodIndex.get(alias, project, LuaPredefinedScope(project))
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

    override fun processStaticMethods(context: SearchContext, processor: (ITyClass, LuaClassMethod) -> Unit) {
        val clazzName = className
        var list = LuaClassMethodIndex.findStaticMethods(clazzName, context)

        val alias = aliasName
        if (alias != null) {
            list += LuaClassMethodIndex.findStaticMethods(alias, context)
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

    override val displayName: String get() = if(isAnonymous) "Anonymous" else className

    override fun lazyInit(searchContext: SearchContext) {
        if (!_lazyInitialized) {
            _lazyInitialized = true
            doLazyInit(searchContext)
        }
    }

    open fun doLazyInit(searchContext: SearchContext) {
        val classDef = LuaClassIndex.find(className, searchContext)
        if (classDef != null && aliasName == null) {
            val tyClass = classDef.type
            aliasName = tyClass.aliasName
            superClassName = tyClass.superClassName
        }
    }

    override fun getSuperClass(context: SearchContext): ITyClass? {
        val clsName = superClassName
        if (clsName != null) {
            val def = LuaClassIndex.find(clsName, context)
            return def?.type
        }
        return null
    }

    override fun findMethod(name: String, searchContext: SearchContext): LuaClassMethod? {
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

    override fun findField(name: String, searchContext: SearchContext): LuaClassField? {
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
        val G: TyClass = TySerializedClass(Constants.WORD_G)

        fun createAnonymousType(nameDef: LuaNameDef): TyClass {
            val tyName = "${nameDef.node.startOffset}@${nameDef.containingFile.name}"
            return TySerializedClass(tyName, null, null, TyFlags.ANONYMOUS)
        }

        fun createGlobalType(nameExpr: LuaNameExpr): TyClass {
            return TySerializedClass(nameExpr.text, null, null, TyFlags.GLOBAL)
        }
    }
}

class TyPsiDocClass(val classDef: LuaDocClassDef) : TyClass(classDef.name) {

    init {
        val supperRef = classDef.superClassNameRef
        if (supperRef != null)
            superClassName = supperRef.text
        aliasName = classDef.aliasName
    }

    override fun doLazyInit(searchContext: SearchContext) {}
}

open class TySerializedClass(name: String, supper: String? = null,
                             alias: String? = null,
                             flags: Int = 0)
    : TyClass(name, supper) {
    init {
        aliasName = alias
        this.flags = flags
    }
}

//todo Lazy class ty
class TyLazyClass(name: String) : TySerializedClass(name)

fun getTableTypeName(table: LuaTableExpr): String {
    val fileName = table.containingFile.name
    return "$fileName@(${table.node.startOffset})table"
}

class TyTable(val table: LuaTableExpr) : TyClass(getTableTypeName(table)) {
    init {
        this.flags = TyFlags.ANONYMOUS
    }
    override fun processFields(context: SearchContext, processor: (ITyClass, LuaClassField) -> Unit) {
        for (field in table.tableFieldList) {
            processor(this, field)
        }
        super.processFields(context, processor)
    }

    override val displayName: String
        get() = "table"

    override fun toString(): String = displayName

    override fun doLazyInit(searchContext: SearchContext) = Unit
}