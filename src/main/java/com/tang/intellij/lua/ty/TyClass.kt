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
import com.tang.intellij.lua.stubs.*

interface ITyClass : ITy {
    val className: String
    val varName: String
    var superClass: ITy?
    var aliasName: String?
    var params: Array<TyParameter>?
    var signatures: Array<IFunSignature>?

    fun processAlias(processor: Processor<String>): Boolean
    fun lazyInit(searchContext: SearchContext)

    fun recoverAlias(context: SearchContext, aliasSubstitutor: TyAliasSubstitutor): ITy {
        return this
    }
}

fun ITyClass.isVisibleInScope(project: Project, contextTy: ITy, visibility: Visibility): Boolean {
    if (visibility == Visibility.PUBLIC)
        return true

    TyUnion.each(contextTy) {
        if (it is ITyClass) {
            if (it == this || (
                            visibility == Visibility.PROTECTED
                            && LuaClassInheritorsSearch.isClassInheritFrom(GlobalSearchScope.projectScope(project), project, className, it.className))
            ) {
                return true
            }
        }
    }

    return false
}

abstract class TyClass(override val className: String,
                       override var params: Array<TyParameter>? = null,
                       override val varName: String = "",
                       override var superClass: ITy? = null,
                       override var signatures: Array<IFunSignature>? = null
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

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean, deep: Boolean): Boolean {
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
            if (!processor(this, member)) {
                return false
            }
        }

        // super
        if (deep) {
            return processSuperClass(this, context) {
                it.processMembers(context, processor, false)
            }
        }

        return true
    }

    override fun processSignatures(context: SearchContext, processor: Processor<IFunSignature>): Boolean {
        lazyInit(context)

        signatures?.let {
            for (signature in it) {
                if (!processor.process(signature)) {
                    return false
                }
            }
        }

        return true
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return LuaShortNamesManager.getInstance(searchContext.project).findMember(this, name, searchContext)
    }

    override fun findIndexer(indexTy: ITy, searchContext: SearchContext): LuaClassMember? {
        return LuaShortNamesManager.getInstance(searchContext.project).findIndexer(this, indexTy, searchContext)
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
                flags = tyClass.flags
                signatures = tyClass.signatures
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

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        lazyInit(context)

        val resolved = TyAliasSubstitutor.substitute(this, context)

        if (resolved != this) {
            return resolved.contravariantOf(other, context, flags)
        } else {
            return super.contravariantOf(other, context, flags)
        }
    }

    companion object {
        // for _G
        val G: TyClass = createSerializedClass(Constants.WORD_G)

        fun createAnonymousType(nameDef: LuaNameDef): TyClass {
            val stub = nameDef.stub
            val tyName = stub?.anonymousType ?: getAnonymousType(nameDef)
            return createSerializedClass(tyName, null, nameDef.name, null, null, null, TyFlags.ANONYMOUS)
        }

        fun createGlobalType(name: String, store: Boolean = false): ITy {
            return createSerializedClass(getGlobalTypeName(name), null, name, null, null, null, TyFlags.GLOBAL)
        }

        fun createGlobalType(nameExpr: LuaNameExpr, store: Boolean): ITy {
            return createGlobalType(nameExpr.name, store)
        }

        fun createSelfType(classTy: ITyClass): TyClass {
            val tyName = getSelfType(classTy)
            return createSerializedClass(tyName, null, Constants.WORD_SELF, classTy, null, null, TyFlags.ANONYMOUS)
        }
    }
}

class TyPsiDocClass(tagClass: LuaDocTagClass) : TyClass(
        tagClass.name,
        tagClass.genericDefList.map { TyParameter(it) }.toTypedArray(),
        "",
        tagClass.superClassRef?.let { Ty.create(it) },
        tagClass.overloads
) {
    init {
        aliasName = tagClass.aliasName
        this.flags = if (tagClass.isShape) TyFlags.SHAPE else 0
    }

    override fun doLazyInit(searchContext: SearchContext) {}
}

open class TySerializedClass(name: String,
                             params: Array<TyParameter>? = null,
                             varName: String = name,
                             superClass: ITy? = null,
                             signatures: Array<IFunSignature>? = null,
                             alias: String? = null,
                             flags: Int = 0)
    : TyClass(name, params, varName, superClass, signatures) {
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
                          signatures: Array<IFunSignature>? = null,
                          alias: String? = null,
                          flags: Int = 0): TyClass {
    val list = name.split("|")
    if (list.size == 3) {
        val type = list[0].toInt()
        if (type == 10) {
            return TySerializedDocTable(name)
        }
    }

    return TySerializedClass(name, params, varName, superClass, signatures, alias, flags)
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

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean, deep: Boolean): Boolean {
        for (field in table.tableFieldList) {
            if (!processor(this, field)) {
                return false
            }
        }
        return super.processMembers(context, processor, deep)
    }

    override fun toString(): String = displayName

    override fun doLazyInit(searchContext: SearchContext) = Unit

    fun toGeneric(context: SearchContext): ITyGeneric {
        if (isEmpty(context)) {
            return TySerializedGeneric(arrayOf(Ty.UNKNOWN, Ty.UNKNOWN), Ty.TABLE)
        }

        var keyType: ITy = Ty.VOID
        var elementType: ITy = Ty.VOID

        processMembers(context) { prefixTy, classMember ->
            val name = classMember.name
            val indexType = classMember.indexType

            if (classMember is LuaTableField) {
                val exprList = classMember.exprList

                if (exprList.size == 2) {
                    keyType = keyType.union(exprList[0].guessType(context))
                    elementType = elementType.union(exprList[1].guessType(context))
                } else if (exprList.size == 1) {
                    if (name != null) {
                        keyType = keyType.union(TyPrimitiveLiteral.getTy(TyPrimitiveKind.String, name))
                    } else {
                        keyType = keyType.union(Ty.NUMBER)
                    }

                    elementType = elementType.union(exprList[0].guessType(context))
                }
            } else if (classMember is LuaIndexExpr) {
                val idExpr = classMember.idExpr

                if (name != null) {
                    keyType = keyType.union(TyPrimitiveLiteral.getTy(TyPrimitiveKind.String, name))
                    elementType = elementType.union(prefixTy.guessMemberType(name, context) ?: Ty.UNKNOWN)
                } else if (idExpr != null) {
                    val indexTy = idExpr.guessType(context)
                    keyType = keyType.union(indexTy)
                    elementType = elementType.union(prefixTy.guessIndexerType(indexTy, context) ?: Ty.UNKNOWN)
                } else {
                    keyType = Ty.UNKNOWN
                    elementType = Ty.UNKNOWN
                    return@processMembers false
                }
            } else if (name != null) {
                keyType = keyType.union(TyPrimitiveLiteral.getTy(TyPrimitiveKind.String, name))
                elementType = elementType.union(classMember.guessType(context))
            } else if (indexType != null) {
                keyType = keyType.union(indexType.getType())
                elementType = elementType.union(classMember.guessType(context))
            } else {
                keyType = Ty.UNKNOWN
                elementType = Ty.UNKNOWN
                return@processMembers false
            }

            true
        }

        return TySerializedGeneric(arrayOf(keyType, elementType), Ty.TABLE)
    }

    fun isEmpty(context: SearchContext): Boolean {
        return processMembers(context) { _, _ -> false }
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

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean, deep: Boolean): Boolean {
        table.tableFieldList.forEach {
            if (!processor(this, it)) {
                return false
            }
        }
        return true
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return table.tableFieldList.firstOrNull { it.name == name }
    }

    override fun findIndexer(indexTy: ITy, searchContext: SearchContext): LuaClassMember? {
        var narrowestClassMember: LuaClassMember? = null
        var narrowestIndexTy: ITy? = null

        table.tableFieldList.forEach {
            val candidateIndexerTy = it.indexType?.getType()

            if (candidateIndexerTy?.contravariantOf(indexTy, searchContext, TyVarianceFlags.STRICT_UNKNOWN) == true) {
                if (narrowestIndexTy?.contravariantOf(candidateIndexerTy, searchContext, TyVarianceFlags.STRICT_UNKNOWN) != false) {
                    narrowestClassMember = it
                    narrowestIndexTy = candidateIndexerTy
                }
            }
        }

        return narrowestClassMember
    }

    // TODO: TyDocTable should implement this. However, there's no sensible way
    //       to do so at present because LuaClassMember inherits from PsiElement.
    /*override fun substitute(substitutor: ITySubstitutor): ITy {
    }*/
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
        val signatures = stream.readSignatureNullable()
        val aliasName = stream.readName()
        return createSerializedClass(StringRef.toString(className),
                params,
                StringRef.toString(varName),
                superClass,
                signatures,
                StringRef.toString(aliasName),
                flags)
    }

    override fun serializeTy(ty: ITyClass, stream: StubOutputStream) {
        stream.writeName(ty.className)
        stream.writeTyParamsNullable(ty.params)
        stream.writeName(ty.varName)
        stream.writeTyNullable(ty.superClass)
        stream.writeSignaturesNullable(ty.signatures)
        stream.writeName(ty.aliasName)
    }
}
