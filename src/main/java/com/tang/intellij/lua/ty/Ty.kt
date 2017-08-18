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
import com.tang.intellij.lua.psi.LuaParamInfo

enum class TyKind {
    Unknown,
    Primitive,
    Array,
    Function,
    Class,
    Union,
    Generic
}
enum class TyPrimitiveKind {
    String,
    Number,
    Boolean
}

interface ITy {

    val isAnonymous: Boolean

    val kind: TyKind

    val displayName: String

    fun union(ty: ITy): ITy

    fun createTypeString(): String

    fun createReturnString(): String
}

abstract class Ty(override val kind: TyKind) : ITy {

    override val isAnonymous: Boolean = false

    override fun union(ty: ITy): ITy {
        return TyUnion.union(this, ty)
    }

    override fun createTypeString(): String {
        val s = toString()
        return if (s.isEmpty()) "any" else s
    }

    override fun createReturnString(): String {
        val s = toString()
        return if (s.isEmpty()) "void" else s
    }

    override fun toString(): String {
        val list = mutableListOf<String>()
        TyUnion.each(this) {
            list.add(it.displayName)
        }
        return list.joinToString("|")
    }

    companion object {

        val UNKNOWN = TyUnknown()
        val BOOLEAN = TyPrimitive(TyPrimitiveKind.Boolean, "boolean")
        val STRING = TyPrimitive(TyPrimitiveKind.String, "string")
        val NUMBER = TyPrimitive(TyPrimitiveKind.Number, "number")

        private fun getPrimitive(mark: Byte): Ty {
            return when (mark.toInt()) {
                TyPrimitiveKind.Boolean.ordinal -> BOOLEAN
                TyPrimitiveKind.String.ordinal -> STRING
                TyPrimitiveKind.Number.ordinal -> NUMBER
                else -> UNKNOWN
            }
        }

        private fun getKind(ordinal: Int): TyKind {
            return TyKind.values().firstOrNull { ordinal == it.ordinal } ?: TyKind.Unknown
        }

        fun isInvalid(ty: ITy?): Boolean {
            return ty == null || ty is TyUnknown
        }

        fun serialize(ty: ITy, stream: StubOutputStream) {
            stream.writeByte(ty.kind.ordinal)
            when(ty) {
                is ITyArray -> {
                    serialize(ty.base, stream)
                }
                is ITyFunction -> {
                    stream.writeByte(ty.params.size)
                    for (param in ty.params) {
                        LuaParamInfo.serialize(param, stream)
                    }
                    serialize(ty.returnTy, stream)
                }
                is ITyClass -> {
                    stream.writeName(ty.className)
                    stream.writeName(ty.superClassName)
                    stream.writeName(ty.aliasName)
                }
                is TyPrimitive -> {
                    stream.writeByte(ty.primitiveKind.ordinal)
                }
                is TyUnion -> {
                    stream.writeByte(ty.size)
                    TyUnion.each(ty) { serialize(it, stream) }
                }
                is ITyGeneric -> {
                    serialize(ty.base, stream)
                    stream.writeByte(ty.params.size)
                    ty.params.forEach { serialize(it, stream) }
                }
            }
        }

        fun deserialize(stream: StubInputStream): ITy {
            val kind = getKind(stream.readByte().toInt())
            return when (kind) {
                TyKind.Array -> {
                    val base = deserialize(stream)
                    TyArray(base)
                }
                TyKind.Function -> {
                    val size = stream.readByte()
                    val arr = mutableListOf<LuaParamInfo>()
                    for (i in 0 until size) {
                        arr.add(LuaParamInfo.deserialize(stream))
                    }
                    val retTy = deserialize(stream)
                    TySerializedFunction(retTy, arr.toTypedArray())
                }
                TyKind.Class -> {
                    val className = stream.readName()
                    val superName = stream.readName()
                    val aliasName = stream.readName()
                    TySerializedClass(StringRef.toString(className), StringRef.toString(superName), StringRef.toString(aliasName))
                }
                TyKind.Primitive -> getPrimitive(stream.readByte())
                TyKind.Union -> {
                    var union:ITy = TyUnion()
                    val size = stream.readByte()
                    for (i in 0 until size) {
                        union = TyUnion.union(union, deserialize(stream))
                    }
                    union
                }
                TyKind.Generic -> {
                    val base = deserialize(stream)
                    val size = stream.readByte()
                    val params = mutableListOf<ITy>()
                    for (i in 0 until size) {
                        params.add(deserialize(stream))
                    }
                    TySerializedGeneric(params.toTypedArray(), base)
                }
                else -> TyUnknown()
            }
        }
    }
}

class TyPrimitive(val primitiveKind: TyPrimitiveKind, override val displayName: String) : Ty(TyKind.Primitive)

interface ITyArray : ITy {
    val base: ITy
}

class TyArray(override val base: ITy) : Ty(TyKind.Array), ITyArray {
    override val displayName: String
        get() = "${base.displayName}[]"
}

class TyUnion : Ty(TyKind.Union) {
    private val children = mutableListOf<ITy>()

    override val displayName: String
        get() = "Union"

    val size:Int
        get() = children.size

    private fun union2(ty: ITy): TyUnion {
        if (ty is TyUnion)
            children.addAll(ty.children)
        else
            children.add(ty)
        return this
    }

    companion object {
        fun <T : ITy> find(ty: ITy, clazz: Class<T>): T? {
            if (clazz.isInstance(ty))
                return clazz.cast(ty)
            var ret: T? = null
            process(ty) {
                if (clazz.isInstance(it)) {
                    ret = clazz.cast(it)
                    return@process false
                }
                true
            }
            return ret
        }

        fun process(ty: ITy, process: (ITy) -> Boolean) {
            if (ty is TyUnion) {
                for (child in ty.children) {
                    if (!process(child))
                        break
                }
            } else process(ty)
        }

        fun each(ty: ITy, process: (ITy) -> Unit) {
            if (ty is TyUnion) {
                ty.children.forEach(process)
            } else process(ty)
        }

        fun union(t1: ITy, t2: ITy): ITy {
            if (t1 is TyUnknown)
                return t2
            else if (t2 is TyUnknown)
                return t1
            else if (t1 is TyUnion)
                return t1.union2(t2)
            else if (t2 is TyUnion)
                return t2.union2(t1)
            else {
                val u = TyUnion()
                u.children.add(t1)
                u.children.add(t2)
                return u
            }
        }

        fun getPrefectClass(ty: ITy): ITyClass? {
            var tc: ITyClass? = null
            process(ty) {
                if (it is ITyClass) {
                    tc = it
                    return@process false
                }
                true
            }
            return tc
        }
    }
}

class TyUnknown : Ty(TyKind.Unknown) {
    override val displayName: String
        get() = "Unknown"
}