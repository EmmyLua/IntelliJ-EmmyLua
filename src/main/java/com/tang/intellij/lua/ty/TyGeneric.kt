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
import com.tang.intellij.lua.comment.psi.LuaDocGenericTy
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.readParamNames
import com.tang.intellij.lua.stubs.writeParamNames

class TyParameter(val name: String, base: String? = null, baseParams: Array<String>? = null) : TySerializedClass(name, emptyArray(), name, base, baseParams) {
    override val kind: TyKind
        get() = TyKind.GenericParam

    override fun contravariantOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return super.contravariantOf(other, context, strict)
                || getSuperClass(context)?.contravariantOf(other, context, strict) != false
    }

    override fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean) {
        val superType = getSuperClass(context) as? ITyClass ?: return
        superType.processMembers(context, processor, deep)
    }

    override fun doLazyInit(searchContext: SearchContext) {}
}

object TyGenericParamSerializer : TySerializer<TyParameter>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): TyParameter {
        val name = StringRef.toString(stream.readName())
        val base = StringRef.toString(stream.readName())
        return TyParameter(name, base, stream.readParamNames())
    }

    override fun serializeTy(ty: TyParameter, stream: StubOutputStream) {
        stream.writeName(ty.name)
        stream.writeName(ty.superClassName)
        stream.writeParamNames(ty.superClassParams)
    }
}

interface ITyGeneric : ITy {
    val params: Array<ITy>
    val base: ITy

    fun getParamTy(index: Int): ITy {
        return params.getOrElse(index) { Ty.UNKNOWN }
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
        return base.getSuperClass(context)
    }

    override fun contravariantOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        if (super.contravariantOf(other, context, strict)) return true

        if (other is ITyArray) {
            return base == Ty.TABLE
                    && params.size == 2
                    && (
                        params[0] == Ty.NUMBER
                        || (params[0] is TyUnknown && !strict)
                    ) && (
                        params[1] == other.base
                        || (!strict && params[1].contravariantOf(other.base, context, strict))
                    )
        }

        var otherBase: ITy? = null
        var otherParams: Array<out ITy>? =  null

        if (other is ITyGeneric) {
            otherBase = other.base
            otherParams = other.params
        } else if (other is ITyClass) {
            otherBase = other
            otherParams = other.params
        } else if ((other == Ty.TABLE || other is TyTable) && base == Ty.TABLE && params.size == 2) {
            if (params[0] is TyUnknown && params[1] is TyUnknown) {
                return true
            }

            if (other is TyTable) {
                val genericTable = other.toGeneric(context)
                otherBase = genericTable.base
                otherParams = genericTable.params
            }
        }

        return otherBase != null && otherParams != null
                && base.contravariantOf(otherBase, context, strict)
                && params.size == otherParams.size
                && params.indices.all { i -> // Params are always invariant as we don't support use-site variance nor immutable/read-only annotations
                    val param = params[i]
                    val otherParam = otherParams[i]
                    return param.equals(otherParam)
                            || param is TyUnknown
                            || (otherParam is TyUnknown && !strict)
                }
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
}

class TyDocGeneric(luaDocGenericTy: LuaDocGenericTy) : TyGeneric() {

    private fun initBaseTy(luaDocGenericTy: LuaDocGenericTy): ITy {
        return luaDocGenericTy.classNameRef.resolveType()
    }

    private val _baseTy:ITy = initBaseTy(luaDocGenericTy)

    private fun initParams(luaDocGenericTy: LuaDocGenericTy): Array<ITy> {
        return luaDocGenericTy.tyList.map { it.getType() }.toTypedArray()
    }

    private val _params: Array<ITy> = initParams(luaDocGenericTy)

    override val params: Array<ITy>
        get() = _params
    override val base: ITy
        get() = _baseTy

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
