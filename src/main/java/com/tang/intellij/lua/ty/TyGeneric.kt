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

import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.comment.psi.LuaDocGenericDef
import com.tang.intellij.lua.comment.psi.LuaDocGenericTy
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.readTyNullable
import com.tang.intellij.lua.stubs.writeTyNullable

fun genericParameterName(def: LuaDocGenericDef): String {
    return "${def.id.text}@${def.node.startOffset}@${def.containingFile.name}"
}

class TyParameter(val name: String, varName: String, superClass: ITy? = null) : TySerializedClass(name, emptyArray(), varName, superClass, null) {
    constructor(def: LuaDocGenericDef) : this(genericParameterName(def), def.id.text, def.classRef?.let { Ty.create(it) })

    override val kind: TyKind
        get() = TyKind.GenericParam

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean, deep: Boolean): Boolean {
        val superType = getSuperClass(context)

        if (superType is ITyClass) {
            return superType.processMembers(context, processor, deep)
        } else if (superType is ITyGeneric) {
            return (superType.base as? ITyClass)?.processMembers(context, processor, deep) ?: true
        }

        return true
    }

    override fun toString(): String {
        return displayName
    }

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        return if (flags and TyVarianceFlags.ABSTRACT_PARAMS != 0) {
            superClass?.contravariantOf(other, context, flags) ?: true
        } else super.contravariantOf(other, context, flags)
    }

    override fun doLazyInit(searchContext: SearchContext) {}
}

object TyGenericParamSerializer : TySerializer<TyParameter>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): TyParameter {
        val name = StringRef.toString(stream.readName())
        val varName = StringRef.toString(stream.readName())
        val superClass = stream.readTyNullable()
        return TyParameter(name, varName, superClass)
    }

    override fun serializeTy(ty: TyParameter, stream: StubOutputStream) {
        stream.writeName(ty.name)
        stream.writeName(ty.varName)
        stream.writeTyNullable(ty.superClass)
    }
}

interface ITyGeneric : ITy {
    val params: Array<ITy>
    val base: ITy

    fun getParamTy(index: Int): ITy {
        return params.elementAtOrNull(index) ?: Ty.UNKNOWN
    }

    fun getParameterSubstitutor(context: SearchContext): TyParameterSubstitutor {
        val paramMap = mutableMapOf<String, ITy>()
        val resolvedBase = TyAliasSubstitutor.substitute(base, context)
        val baseParams = resolvedBase.getParams(context)

        baseParams?.forEachIndexed { index, baseParam ->
            if (index < params.size) {
                paramMap[baseParam.varName] = params[index]
            }
        }

        return TyParameterSubstitutor(paramMap)
    }
}

abstract class TyGeneric(final override val params: Array<ITy>, final override val base: ITy) : Ty(TyKind.Generic), ITyGeneric {

    override fun equals(other: Any?): Boolean {
        return other is ITyGeneric && other.base == base && other.displayName == displayName
    }

    override fun hashCode(): Int {
        return displayName.hashCode()
    }

    override fun getSuperClass(context: SearchContext): ITy? {
        return base.getSuperClass(context)?.substitute(getParameterSubstitutor(context))
    }

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        val resolvedBase = TyAliasSubstitutor.substitute(base, context)

        if (resolvedBase is ITyAlias) {
            TyUnion.each(resolvedBase.ty.substitute(getParameterSubstitutor(context))) {
                if (it.contravariantOf(other, context, flags)) {
                    return true
                }
            }

            return false
        }

        if (resolvedBase is ITyClass) {
            resolvedBase.lazyInit(context)

            if (resolvedBase.flags and TyFlags.SHAPE != 0) {
                val parameterSubstitutor = getParameterSubstitutor(context)
                return processMembers(context, { _, classMember ->
                    val memberName = classMember.name

                    if (memberName == null) {
                        return@processMembers false
                    }

                    val memberTy = classMember.guessType(context).substitute(parameterSubstitutor)
                    val otherMember = other.findMember(memberName, context)

                    if (otherMember == null) {
                        return@processMembers TyUnion.find(memberTy, TyNil::class.java) != null
                    }

                    val otherMemberTy = otherMember.guessType(context)

                    memberTy.contravariantOf(otherMemberTy, context, flags)
                }, true)
            }
        }

        if (other is ITyArray) {
            return if (resolvedBase == Ty.TABLE && params.size == 2) {
                val keyTy = params.first()
                val valueTy = params.last()
                return (keyTy == Ty.NUMBER || (keyTy is TyUnknown && flags and TyVarianceFlags.STRICT_UNKNOWN == 0))
                        && (valueTy == other.base || (flags and TyVarianceFlags.WIDEN_TABLES != 0 && valueTy.contravariantOf(other.base, context, flags)))
            } else false
        }

        var otherBase: ITy? = null
        var otherParams: Array<out ITy>? =  null
        var contravariantParams = false

        if (other is ITyGeneric) {
            otherBase = other.base
            otherParams = other.params
        } else if ((other == Ty.TABLE || other is TyTable) && resolvedBase == Ty.TABLE && params.size == 2) {
            val keyTy = params.first()
            val valueTy = params.last()

            if (keyTy is TyUnknown && valueTy is TyUnknown) {
                return true
            }

            if (other is TyTable) {
                val genericTable = other.toGeneric(context)
                otherBase = genericTable.base
                otherParams = genericTable.params
                contravariantParams = flags and TyVarianceFlags.WIDEN_TABLES != 0
            }
        } else if (other is ITyClass) {
            otherBase = other
            otherParams = other.getParams(context)
        }

        if (otherBase != null && otherParams != null
                && resolvedBase.contravariantOf(otherBase, context, flags)
                && params.size == otherParams.size
                && params.asSequence().zip(otherParams.asSequence()).all { (param, otherParam) ->
                    // Params are always invariant as we don't support use-site variance nor immutable/read-only annotations
                    param.equals(otherParam)
                            || param is TyUnknown
                            || (flags and TyVarianceFlags.STRICT_UNKNOWN == 0 && otherParam is TyUnknown)
                            || ((contravariantParams || (flags and TyVarianceFlags.ABSTRACT_PARAMS != 0 && param is TyParameter))
                                && param.contravariantOf(otherParam, context, flags))
                }) {
            return true
        }

        return super.contravariantOf(other, context, flags)
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitGeneric(this)
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        return substitutor.substitute(this)
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return base.findMember(name, searchContext)
    }

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean, deep: Boolean): Boolean {
        if (!base.processMembers(context, { _, classMember -> processor(this, classMember) }, false)) {
            return false
        }

        // super
        if (deep) {
            return Ty.processSuperClass(this, context) {
                it.processMembers(context, processor, false)
            }
        }

        return true
    }
}

class TyDocGeneric(luaDocGenericTy: LuaDocGenericTy) : TyGeneric(luaDocGenericTy.tyList.map { it.getType() }.toTypedArray(), luaDocGenericTy.classNameRef.resolveType())

class TySerializedGeneric(params: Array<ITy>, base: ITy) : TyGeneric(params, base)

object TyGenericSerializer : TySerializer<ITyGeneric>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): ITyGeneric {
        val base = Ty.deserialize(stream)
        val size = stream.readByte()
        val params = mutableListOf<ITy>()
        for (i in 0 until size) {
            params.add(Ty.deserialize(stream))
        }
        return TySerializedGeneric(params.toTypedArray(), base)
    }

    override fun serializeTy(ty: ITyGeneric, stream: StubOutputStream) {
        Ty.serialize(ty.base, stream)
        stream.writeByte(ty.params.size)
        ty.params.forEach { Ty.serialize(it, stream) }
    }
}
