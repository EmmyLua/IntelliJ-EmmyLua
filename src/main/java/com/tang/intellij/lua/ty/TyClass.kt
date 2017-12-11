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

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.search.LuaClassInheritorsSearch
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
    fun findMemberType(name: String, searchContext: SearchContext): ITy?
    fun findSuperMember(name: String, searchContext: SearchContext): LuaClassMember?
}

fun ITyClass.isVisibleInScope(project: Project, contextTy: ITy, visibility: Visibility): Boolean {
    if (visibility == Visibility.PUBLIC)
        return true
    var isVisible = false
    TyUnion.process(contextTy) {
        if (it is ITyClass) {
            if (it == this)
                isVisible = true
            else if (visibility == Visibility.PROTECTED) {
                isVisible = LuaClassInheritorsSearch.isClassInheritFrom(GlobalSearchScope.projectScope(project), project, className, it.className)
            }
        }
        !isVisible
    }
    return isVisible
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
            if (superType is TyClass) superType.processMembers(context, processor, deep)
        }
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return LuaClassMemberIndex.find(this, name, searchContext)
    }

    override fun findMemberType(name: String, searchContext: SearchContext): ITy? {
        return findMember(name, searchContext)?.guessType(searchContext)
    }

    override fun findSuperMember(name: String, searchContext: SearchContext): LuaClassMember? {
        // Travel up the hierarchy to find the lowest member of this type on a superclass (excluding this class)
        var superClass = getSuperClass(searchContext)
        while (superClass != null && superClass is TyClass) {
            val member = superClass.findMember(name, searchContext)
            if (member != null) return member
            superClass = superClass.getSuperClass(searchContext)
        }
        return null
    }

    override val displayName: String get() = when {
        isAnonymous -> "Anonymous"
        isGlobal -> "Global"
        else -> className
    }

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

    override fun getSuperClass(context: SearchContext): ITy? {
        val clsName = superClassName
        if (clsName != null) {
            return when (clsName){
                Constants.WORD_NIL -> Ty.NIL
                Constants.WORD_ANY -> Ty.UNKNOWN
                Constants.WORD_BOOLEAN -> Ty.BOOLEAN
                Constants.WORD_STRING -> Ty.STRING
                Constants.WORD_NUMBER -> Ty.NUMBER
                Constants.WORD_TABLE -> Ty.TABLE
                Constants.WORD_FUNCTION -> Ty.FUNCTION
                else -> LuaClassIndex.find(clsName, context)?.type
            }
        }
        return null
    }

    override fun subTypeOf(other: ITy, context: SearchContext): Boolean {
        if (super.subTypeOf(other, context)) return true

        // Lazy init for superclass
        this.doLazyInit(context)
        // Check if any of the superclasses are type
        var superClass = getSuperClass(context)
        while (superClass != null) {
            if (other == superClass) return true
            superClass = superClass.getSuperClass(context)
        }

        return false
    }

    companion object {
        // for _G
        val G: TyClass = TySerializedClass(Constants.WORD_G)

        fun createAnonymousType(nameDef: LuaNameDef): TyClass {
            val stub = nameDef.stub
            val tyName = if (stub != null) stub.anonymousType else getAnonymousType(nameDef)
            return TySerializedClass(tyName, null, null, TyFlags.ANONYMOUS)
        }

        fun createGlobalType(nameExpr: LuaNameExpr): TyClass =
                TySerializedClass(getGlobalTypeName(nameExpr), null, null, TyFlags.GLOBAL)
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

open class TySerializedClass(name: String,
                             supper: String? = null,
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
    val stub = table.stub
    if (stub != null)
        return stub.tableTypeName

    val fileName = table.containingFile.name
    return "$fileName@(${table.node.startOffset})table"
}

fun getAnonymousType(nameDef: LuaNameDef): String {
    return "${nameDef.node.startOffset}@${nameDef.containingFile.name}"
}

fun getGlobalTypeName(expr: LuaNameExpr): String {
    val text = expr.name
    return if (text == Constants.WORD_G) text else "__$text"
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

    override fun subTypeOf(other: ITy, context: SearchContext): Boolean {
        // Empty list is a table, but subtype of all lists
        return super.subTypeOf(other, context) || other == Ty.TABLE || (other is TyArray && table.tableFieldList.size == 0)
    }
}