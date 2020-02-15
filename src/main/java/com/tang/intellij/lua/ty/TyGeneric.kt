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

class TyParameter(val name: String, varName: String, superClass: ITy? = null) : TySerializedClass(name, emptyArray(), varName, superClass, null, TyFlags.ANONYMOUS) {

    constructor(def: LuaDocGenericDef) : this(def.id.text, def.id.text, def.classRef?.let { Ty.create(it) })

    override val kind: TyKind
        get() = TyKind.GenericParam

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Unit, deep: Boolean) {
        val superType = getSuperClass(context)

        if (superType is ITyClass) {
            superType.processMembers(context, processor, deep)
        } else if (superType is ITyGeneric) {
            (superType.base as? ITyClass)?.processMembers(context, processor, deep)
        }
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
}

abstract class TyGeneric : Ty(TyKind.Generic), ITyGeneric {

    override fun equals(other: Any?): Boolean {
        return other is ITyGeneric && other.base == base && other.displayName == displayName
    }

    override fun hashCode(): Int {
        return displayName.hashCode()
    }

    override fun getSuperClass(context: SearchContext): ITy? {
        val baseParams = base.getParams(context)
        var superClass = base.getSuperClass(context)

        if (baseParams != null && superClass is ITyGeneric) {
            val paramMap = mutableMapOf<String, ITy>()

            baseParams.forEachIndexed { index, baseParam ->
                if (index < params.size) {
                    paramMap[baseParam.varName] = params[index]
                }
            }

            superClass = superClass.substitute(TyParameterSubstitutor(paramMap))
        }

        return superClass
    }

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        if (other is ITyArray) {
            return if (base == Ty.TABLE && params.size == 2) {
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
        } else if ((other == Ty.TABLE || other is TyTable) && base == Ty.TABLE && params.size == 2) {
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
                && base.contravariantOf(otherBase, context, flags)
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
        return TySerializedGeneric(
                params.map { it.substitute(substitutor) }.toTypedArray(),
                base.substitute(substitutor)
        )
    }

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Unit, deep: Boolean) {
        base.processMembers(context, { _, classMember ->
            processor(this, classMember)
        }, false)

        // super
        if (deep) {
            Ty.processSuperClass(this, context) {
                it.processMembers(context, processor, false)
                true
            }
        }
    }
}

class TyDocGeneric(luaDocGenericTy: LuaDocGenericTy) : TyGeneric() {

    override val base: ITy
    override val params: Array<ITy>

    init {
        var base = luaDocGenericTy.classNameRef.resolveType()
        params = luaDocGenericTy.tyList.map { it.getType() }.toTypedArray()

        if (base is TyClass) {
            val baseParams = base.getParams(SearchContext.get(luaDocGenericTy.project))

            if (baseParams != null && baseParams.isNotEmpty()) {
                val paramMap = mutableMapOf<String, ITy>()

                baseParams.forEachIndexed { index, baseParam ->
                    if (index < params.size) {
                        paramMap[baseParam.varName] = params[index]
                    }
                }

                base = base.substitute(TyParameterSubstitutor(paramMap))
            }
        }

        this.base = base
    }
}

class TySerializedGeneric(override val params: Array<ITy>, override val base: ITy) : TyGeneric()

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
