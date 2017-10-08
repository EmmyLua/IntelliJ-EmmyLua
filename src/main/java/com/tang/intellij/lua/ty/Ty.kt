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
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.search.SearchContext

enum class TyKind {
    Unknown,
    Primitive,
    Array,
    Function,
    Class,
    Union,
    Generic,
    Nil
}
enum class TyPrimitiveKind {
    String,
    Number,
    Boolean,
    Table,
    Function
}
class TyFlags {
    companion object {
        val ANONYMOUS = 0x1
        val GLOBAL = 0x2
        val SELF_FUNCTION = 0x4 // xxx.method()
    }
}

interface ITy {
    val kind: TyKind

    val displayName: String

    val flags: Int

    fun union(ty: ITy): ITy

    fun createTypeString(): String

    fun subTypeOf(other: ITy, context: SearchContext): Boolean

    fun getSuperClass(context: SearchContext): ITy?
}

fun ITy.hasFlag(flag: Int): Boolean = flags and flag == flag

val ITy.isGlobal: Boolean
    get() = hasFlag(TyFlags.GLOBAL)

val ITy.isAnonymous: Boolean
    get() = hasFlag(TyFlags.ANONYMOUS)

abstract class Ty(override val kind: TyKind) : ITy {

    override final var flags: Int = 0

    fun addFlag(flag: Int) {
        flags = flags or flag
    }

    override fun union(ty: ITy): ITy {
        return TyUnion.union(this, ty)
    }

    override fun createTypeString(): String {
        val s = toString()
        return if (s.isEmpty()) Constants.WORD_ANY else s
    }

    override fun toString(): String {
        val list = mutableListOf<String>()
        TyUnion.each(this) { //尽量不使用Global
            if (!it.isAnonymous && !(it is ITyClass && it.isGlobal))
                list.add(it.displayName)
        }
        if (list.isEmpty()) { //使用Global
            TyUnion.each(this) {
                if (!it.isAnonymous && (it is ITyClass && it.isGlobal))
                    list.add(it.displayName)
            }
        }
        return list.joinToString("|")
    }

    override fun subTypeOf(other: ITy, context: SearchContext): Boolean {
        // Everything is subset of any
        if (other.kind == TyKind.Unknown) return true

        // Handle unions, subtype if subtype of any of the union components.
        if (other is TyUnion) return other.getChildTypes().any({ type -> subTypeOf(type, context) })

        // Classes are equal
        return this == other
    }

    override fun getSuperClass(context: SearchContext): ITy? {
        return null
    }

    companion object {

        val UNKNOWN = TyUnknown()
        val BOOLEAN = TyPrimitive(TyPrimitiveKind.Boolean, "boolean")
        val STRING = TyPrimitive(TyPrimitiveKind.String, "string")
        val NUMBER = TyPrimitive(TyPrimitiveKind.Number, "number")
        val TABLE = TyPrimitive(TyPrimitiveKind.Table, "table")
        val FUNCTION = TyPrimitive(TyPrimitiveKind.Function, "function")
        val NIL = TyNil()

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
            stream.writeInt(ty.flags)
            when(ty) {
                is ITyArray -> {
                    serialize(ty.base, stream)
                }
                is ITyFunction -> {
                    FunSignature.serialize(ty.mainSignature, stream)
                    stream.writeByte(ty.signatures.size)
                    for (sig in ty.signatures) {
                        FunSignature.serialize(sig, stream)
                    }
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
            val flags = stream.readInt()
            return when (kind) {
                TyKind.Array -> {
                    val base = deserialize(stream)
                    TyArray(base)
                }
                TyKind.Function -> {
                    val mainSig = FunSignature.deserialize(stream)
                    val size = stream.readByte()
                    val arr = mutableListOf<IFunSignature>()
                    for (i in 0 until size) {
                        arr.add(FunSignature.deserialize(stream))
                    }
                    TySerializedFunction(mainSig, arr.toTypedArray(), flags)
                }
                TyKind.Class -> {
                    val className = stream.readName()
                    val superName = stream.readName()
                    val aliasName = stream.readName()
                    TySerializedClass(StringRef.toString(className),
                            StringRef.toString(superName),
                            StringRef.toString(aliasName),
                            flags)
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

class TyPrimitive(val primitiveKind: TyPrimitiveKind, override val displayName: String) : Ty(TyKind.Primitive) {
    override fun equals(other: Any?): Boolean {
        return other is TyPrimitive && other.primitiveKind == primitiveKind
    }

    override fun hashCode(): Int {
        return primitiveKind.hashCode()
    }
}

interface ITyArray : ITy {
    val base: ITy
}

class TyArray(override val base: ITy) : Ty(TyKind.Array), ITyArray {
    override val displayName: String
        get() = "${base.displayName}[]"

    override fun equals(other: Any?): Boolean {
        return other is ITyArray && base == other.base
    }

    override fun hashCode(): Int {
        return displayName.hashCode()
    }

    override fun subTypeOf(other: ITy, context: SearchContext): Boolean {
        return super.subTypeOf(other, context) || (other is TyArray && base.subTypeOf(other.base, context)) || other == Ty.TABLE
    }
}

class TyUnion : Ty(TyKind.Union) {
    private val childSet = mutableSetOf<ITy>()
    fun getChildTypes() = childSet

    override val displayName: String
        get() = childSet.joinToString("|", transform = { type:ITy -> type.displayName })

    val size:Int
        get() = childSet.size

    private fun union2(ty: ITy): TyUnion {
        if (ty is TyUnion) {
            ty.childSet.forEach { addChild(it) }
        }
        else addChild(ty)
        return this
    }

    private fun addChild(ty: ITy): Boolean {
        return childSet.add(ty)
    }

    // All members of union must be subset of other type
    override fun subTypeOf(other: ITy, context: SearchContext): Boolean {
        return super.subTypeOf(other, context) || childSet.all({ type -> type.subTypeOf(other, context) })
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
                for (child in ty.childSet) {
                    if (!process(child))
                        break
                }
            } else process(ty)
        }

        fun each(ty: ITy, process: (ITy) -> Unit) {
            if (ty is TyUnion) {
                ty.childSet.forEach(process)
            } else process(ty)
        }

        fun union(t1: ITy, t2: ITy): ITy {
            return when {
                t1 is TyUnknown -> t2
                t2 is TyUnknown -> t1
                t1 is TyUnion -> t1.union2(t2)
                t2 is TyUnion -> t2.union2(t1)
                else -> {
                    val u = TyUnion()
                    u.addChild(t1)
                    u.addChild(t2)
                    //if t1 == t2
                    if (u.childSet.size == 1) t1 else u
                }
            }
        }

        fun getPrefectClass(ty: ITy): ITyClass? {
            var tc: ITyClass? = null
            var anonymous: ITyClass? = null
            process(ty) {
                if (it is ITyClass) {
                    if (it.isAnonymous)
                        anonymous = it
                    else {
                        tc = it
                        return@process false
                    }
                }
                true
            }
            return tc ?: anonymous
        }
    }
}

class TyUnknown : Ty(TyKind.Unknown) {
    override val displayName: String
        get() = Constants.WORD_ANY

    override fun equals(other: Any?): Boolean {
        return other is TyUnknown
    }

    override fun hashCode(): Int {
        return Constants.WORD_ANY.hashCode()
    }
}

class TyNil : Ty(TyKind.Nil) {
    override val displayName: String
        get() = Constants.WORD_NIL

    override fun subTypeOf(other: ITy, context: SearchContext): Boolean {

        return super.subTypeOf(other, context) || other is TyNil || !LuaSettings.instance.isNilStrict
    }
}