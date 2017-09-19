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
import com.tang.intellij.lua.stubs.index.LuaClassIndex
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex

interface ITyClass : ITy {
    val className: String
    var superClassName: String?
    var aliasName: String?
    fun lazyInit(searchContext: SearchContext)
    fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean = true)
    fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit) {
        processMembers(context, processor, true)
    }
    fun findMember(name: String, searchContext: SearchContext): LuaClassMember?
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

    override fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean) {
        val clazzName = className
        val project = context.project

        val memberIndex = LuaClassMemberIndex.instance
        val list = memberIndex.get(clazzName.hashCode(), project, LuaPredefinedScope(project))

        val alias = aliasName
        if (alias != null) {
            val classMembers = memberIndex.get(alias.hashCode(), project, LuaPredefinedScope(project))
            list.addAll(classMembers)
        }

        for (member in list) {
            processor(this, member)
        }

        // super
        if (deep) {
            val superType = getSuperClass(context)
            superType?.processMembers(context, processor, deep)
        }
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return LuaClassMemberIndex.find(this, name, searchContext)
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

    override fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean) {
        for (field in table.tableFieldList) {
            processor(this, field)
        }
        super.processMembers(context, processor, deep)
    }

    override val displayName: String
        get() = "table"

    override fun toString(): String = displayName

    override fun doLazyInit(searchContext: SearchContext) = Unit
}