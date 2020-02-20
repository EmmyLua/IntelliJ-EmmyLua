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
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.Processor
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.LuaDocTableDef
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.search.LuaClassInheritorsSearch
import com.tang.intellij.lua.psi.search.LuaShortNamesManager
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.readTyNullable
import com.tang.intellij.lua.stubs.readTyParamsNullable
import com.tang.intellij.lua.stubs.writeTyNullable
import com.tang.intellij.lua.stubs.writeTyParamsNullable

interface ITyClass : ITy {
    val className: String
    val varName: String
    var superClass: ITy?
    var aliasName: String?
    var params: Array<TyParameter>?
    fun processAlias(processor: Processor<String>): Boolean
    fun lazyInit(searchContext: SearchContext)

    fun recoverAlias(context: SearchContext, aliasSubstitutor: TyAliasSubstitutor): ITy {
        return this
    }
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

abstract class TyClass(override val className: String,
                       override var params: Array<TyParameter>? = null,
                       override val varName: String = "",
                       override var superClass: ITy? = null
) : Ty(TyKind.Class), ITyClass {

    final override var aliasName: String? = null

    private var _lazyInitialized: Boolean = false

    override fun equals(other: Any?): Boolean {
        return other is ITyClass && other.className == className && other.flags == flags
    }

    override fun hashCode(): Int {
        return className.hashCode()
    }

    override fun processAlias(processor: Processor<String>): Boolean {
        val alias = aliasName
        if (alias == null || alias == className)
            return true
        if (!processor.process(alias))
            return false
        if (!isGlobal && !isAnonymous && LuaSettings.instance.isRecognizeGlobalNameAsType)
            return processor.process(getGlobalTypeName(className))
        return true
    }

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Unit, deep: Boolean) {
        lazyInit(context)

        val clazzName = className
        val project = context.project

        val manager = LuaShortNamesManager.getInstance(project)
        val members = manager.getClassMembers(clazzName, context)
        val list = mutableListOf<LuaClassMember>()
        list.addAll(members)

        processAlias(Processor { alias ->
            val classMembers = manager.getClassMembers(alias, context)
            list.addAll(classMembers)
        })

        for (member in list) {
            processor(this, member)
        }

        // super
        if (deep) {
            processSuperClass(this, context) {
                it.processMembers(context, processor, false)
                true
            }
        }
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return LuaShortNamesManager.getInstance(searchContext.project).findMember(this, name, searchContext)
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitClass(this)
    }

    override fun lazyInit(searchContext: SearchContext) {
        if (!_lazyInitialized) {
            _lazyInitialized = true
            doLazyInit(searchContext)
        }
    }

    open fun doLazyInit(searchContext: SearchContext) {
        if (aliasName == null) {
            val classDef = LuaPsiTreeUtil.findClass(className, searchContext)
            if (classDef != null) {
                val tyClass = classDef.type
                aliasName = tyClass.aliasName
                superClass = tyClass.superClass
                params = tyClass.params
            }
        }
    }

    override fun getSuperClass(context: SearchContext): ITy? {
        lazyInit(context)
        return superClass
    }

    override fun getParams(context: SearchContext): Array<TyParameter>? {
        lazyInit(context)
        return params
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        return substitutor.substitute(this)
    }

    companion object {
        // for _G
        val G: TyClass = createSerializedClass(Constants.WORD_G)

        fun createAnonymousType(nameDef: LuaNameDef): TyClass {
            val stub = nameDef.stub
            val tyName = stub?.anonymousType ?: getAnonymousType(nameDef)
            return createSerializedClass(tyName, null, nameDef.name, null, null, TyFlags.ANONYMOUS)
        }

        fun createGlobalType(name: String, store: Boolean = false): ITy {
            val g = createSerializedClass(getGlobalTypeName(name), null, name, null, null, TyFlags.GLOBAL)
            if (!store && LuaSettings.instance.isRecognizeGlobalNameAsType)
                return createSerializedClass(name, null, name, null, null, TyFlags.GLOBAL).union(g)
            return g
        }

        fun createGlobalType(nameExpr: LuaNameExpr, store: Boolean): ITy {
            return createGlobalType(nameExpr.name, store)
        }

        fun createSelfType(classTy: ITyClass): TyClass {
            val tyName = getSelfType(classTy)
            return createSerializedClass(tyName, null, Constants.WORD_SELF, classTy, null, TyFlags.ANONYMOUS)
        }
    }
}

class TyPsiDocClass(tagClass: LuaDocTagClass) : TyClass(tagClass.name, tagClass.genericDefList.map { TyParameter(it) }.toTypedArray(), "", tagClass.superClassRef?.let { Ty.create(it) }) {

    init {
        aliasName = tagClass.aliasName
    }

    override fun doLazyInit(searchContext: SearchContext) {}
}

open class TySerializedClass(name: String,
                             params: Array<TyParameter>? = null,
                             varName: String = name,
                             superClass: ITy? = null,
                             alias: String? = null,
                             flags: Int = 0)
    : TyClass(name, params, varName, superClass) {
    init {
        aliasName = alias
        this.flags = flags
    }

    override fun recoverAlias(context: SearchContext, aliasSubstitutor: TyAliasSubstitutor): ITy {
        if (this.isAnonymous || this.isGlobal)
            return this
        val alias = LuaShortNamesManager.getInstance(context.project).findAlias(className, context)
        return alias?.type?.substitute(aliasSubstitutor) ?: this
    }
}

//todo Lazy class ty
class TyLazyClass(name: String, params: Array<TyParameter>? = null) : TySerializedClass(name, params)

fun createSerializedClass(name: String,
                          params: Array<TyParameter>? = null,
                          varName: String = name,
                          superClass: ITy? = null,
                          alias: String? = null,
                          flags: Int = 0): TyClass {
    val list = name.split("|")
    if (list.size == 3) {
        val type = list[0].toInt()
        if (type == 10) {
            return TySerializedDocTable(name)
        }
    }

    return TySerializedClass(name, params, varName, superClass, alias, flags)
}

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

fun getSelfType(classTy: ITyClass): String {
    return "${classTy.className}:${Constants.WORD_SELF}"
}

fun getGlobalTypeName(text: String): String {
    return if (text == Constants.WORD_G) text else "$$text"
}

fun getGlobalTypeName(nameExpr: LuaNameExpr): String {
    return getGlobalTypeName(nameExpr.name)
}

class TyTable(val table: LuaTableExpr) : TyClass(getTableTypeName(table)) {
    init {
        this.flags = TyFlags.ANONYMOUS or TyFlags.ANONYMOUS_TABLE
    }

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Unit, deep: Boolean) {
        for (field in table.tableFieldList) {
            processor(this, field)
        }
        super.processMembers(context, processor, deep)
    }

    override fun toString(): String = displayName

    override fun doLazyInit(searchContext: SearchContext) = Unit

    fun toGeneric(context: SearchContext): ITyGeneric {
        var keyType: ITy = Ty.VOID
        var elementType: ITy = Ty.VOID

        table.tableFieldList.forEach {
            val exprList = it.exprList

            if (exprList.size == 2) {
                keyType = keyType.union(exprList[0].guessType(context))
                elementType = elementType.union(exprList[1].guessType(context))
            } else {
                keyType = keyType.union(if (it.id != null) Ty.STRING else Ty.NUMBER)
                elementType = elementType.union(exprList[0].guessType(context))
            }
        }

        return TySerializedGeneric(arrayOf(keyType, elementType), Ty.TABLE)
    }
}

fun getDocTableTypeName(table: LuaDocTableDef): String {
    val stub = table.stub
    if (stub != null)
        return stub.className

    val fileName = table.containingFile.name
    return "10|$fileName|${table.node.startOffset}"
}

class TyDocTable(val table: LuaDocTableDef) : TyClass(getDocTableTypeName(table)) {
    override fun doLazyInit(searchContext: SearchContext) {}

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Unit, deep: Boolean) {
        table.tableFieldList.forEach {
            processor(this, it)
        }
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return table.tableFieldList.firstOrNull { it.name == name }
    }
}

class TySerializedDocTable(name: String) : TySerializedClass(name) {
    override fun recoverAlias(context: SearchContext, aliasSubstitutor: TyAliasSubstitutor): ITy {
        return this
    }
}

object TyClassSerializer : TySerializer<ITyClass>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): ITyClass {
        val className = stream.readName()
        val params = stream.readTyParamsNullable()
        val varName = stream.readName()
        val superClass = stream.readTyNullable()
        val aliasName = stream.readName()
        return createSerializedClass(StringRef.toString(className),
                params,
                StringRef.toString(varName),
                superClass,
                StringRef.toString(aliasName),
                flags)
    }

    override fun serializeTy(ty: ITyClass, stream: StubOutputStream) {
        stream.writeName(ty.className)
        stream.writeTyParamsNullable(ty.params)
        stream.writeName(ty.varName)
        stream.writeTyNullable(ty.superClass)
        stream.writeName(ty.aliasName)
    }
}
