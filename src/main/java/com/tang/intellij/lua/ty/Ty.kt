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
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.readSignatures
import com.tang.intellij.lua.stubs.writeSignatures

enum class TyKind {
    Unknown,
    Primitive,
    Array,
    Function,
    Class,
    Union,
    Generic,
    Nil,
    Void,
    Tuple,
    GenericParam,
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
        const val ANONYMOUS = 0x1
        const val GLOBAL = 0x2
        const val SELF_FUNCTION = 0x4 // xxx.method()
        const val ANONYMOUS_TABLE = 0x8 // local xx = {}, flag of this table `{}`
    }
}

interface ITy : Comparable<ITy> {
    val kind: TyKind

    val displayName: String

    val flags: Int

    fun union(ty: ITy): ITy

    @Deprecated("use `displayName` instead")
    fun createTypeString(): String

    fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean

    fun getSuperClass(context: SearchContext): ITy?

    fun visitSuper(searchContext: SearchContext, processor: Processor<ITyClass>)

    fun substitute(substitutor: ITySubstitutor): ITy

    fun each(fn: (ITy) -> Unit) {
        TyUnion.each(this, fn)
    }

    fun eachTopClass(fn: Processor<ITyClass>)

    fun accept(visitor: ITyVisitor)

    fun acceptChildren(visitor: ITyVisitor)
}

fun ITy.hasFlag(flag: Int): Boolean = flags and flag == flag

val ITy.isGlobal: Boolean
    get() = hasFlag(TyFlags.GLOBAL)

val ITy.isAnonymous: Boolean
    get() = hasFlag(TyFlags.ANONYMOUS)

private val ITy.worth: Float get() {
    var value = 10f
    when(this) {
        is ITyArray, is ITyGeneric -> value = 80f
        is ITyPrimitive -> value = 70f
        is ITyFunction -> value = 60f
        is ITyClass -> {
            value = when {
                this is TyTable -> 9f
                this.isAnonymous -> 2f
                this.isGlobal -> 5f
                else -> 90f
            }
        }
    }
    return value
}

abstract class Ty(override val kind: TyKind) : ITy {

    final override var flags: Int = 0

    override val displayName: String
        get() = TyRenderer.SIMPLE.render(this)

    fun addFlag(flag: Int) {
        flags = flags or flag
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitTy(this)
    }

    override fun acceptChildren(visitor: ITyVisitor) {
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

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        // Everything is subset of any
        if (other.kind == TyKind.Unknown) return !strict

        // Handle unions, subtype if subtype of any of the union components.
        if (other is TyUnion) return other.getChildTypes().any { type -> subTypeOf(type, context, strict) }

        // Classes are equal
        return this == other
    }

    override fun getSuperClass(context: SearchContext): ITy? {
        return null
    }

    override fun visitSuper(searchContext: SearchContext, processor: Processor<ITyClass>) {
        val superType = getSuperClass(searchContext) as? ITyClass ?: return
        if (processor.process(superType))
            superType.visitSuper(searchContext, processor)
    }

    override fun compareTo(other: ITy): Int {
        return other.worth.compareTo(worth)
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        return substitutor.substitute(this)
    }

    override fun eachTopClass(fn: Processor<ITyClass>) {
        when (this) {
            is ITyClass -> fn.process(this)
            is TyUnion -> {
                ContainerUtil.process(getChildTypes()) {
                    if (it is ITyClass && !fn.process(it))
                        return@process false
                    true
                }
            }
            is TyTuple -> {
                list.firstOrNull()?.eachTopClass(fn)
            }
        }
    }

    companion object {

        val UNKNOWN = TyUnknown()
        val VOID = TyVoid()
        val BOOLEAN = TyPrimitive(TyPrimitiveKind.Boolean, "boolean")
        val STRING = TyPrimitiveClass(TyPrimitiveKind.String, "string")
        val NUMBER = TyPrimitive(TyPrimitiveKind.Number, "number")
        val TABLE = TyPrimitive(TyPrimitiveKind.Table, "table")
        val FUNCTION = TyPrimitive(TyPrimitiveKind.Function, "function")
        val NIL = TyNil()

        private fun getPrimitive(mark: Byte): Ty {
            return when (mark.toInt()) {
                TyPrimitiveKind.Boolean.ordinal -> BOOLEAN
                TyPrimitiveKind.String.ordinal -> STRING
                TyPrimitiveKind.Number.ordinal -> NUMBER
                TyPrimitiveKind.Table.ordinal -> TABLE
                TyPrimitiveKind.Function.ordinal -> FUNCTION
                else -> UNKNOWN
            }
        }

        private fun getKind(ordinal: Int): TyKind {
            return TyKind.values().firstOrNull { ordinal == it.ordinal } ?: TyKind.Unknown
        }

        fun getBuiltin(name: String): ITy? {
            return when (name) {
                Constants.WORD_NIL -> Ty.NIL
                Constants.WORD_VOID -> Ty.VOID
                Constants.WORD_ANY -> Ty.UNKNOWN
                Constants.WORD_BOOLEAN -> Ty.BOOLEAN
                Constants.WORD_STRING -> Ty.STRING
                Constants.WORD_NUMBER -> Ty.NUMBER
                Constants.WORD_TABLE -> Ty.TABLE
                Constants.WORD_FUNCTION -> Ty.FUNCTION
                else -> null
            }
        }

        fun create(name: String): ITy {
            return getBuiltin(name) ?: TyLazyClass(name)
        }

        fun isInvalid(ty: ITy?): Boolean {
            return ty == null || ty is TyUnknown || ty is TyNil || ty is TyVoid
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
                    stream.writeSignatures(ty.signatures)
                }
                is TyParameter -> {
                    stream.writeName(ty.name)
                    stream.writeName(ty.superClassName)
                }
                is ITyPrimitive -> {
                    stream.writeByte(ty.primitiveKind.ordinal)
                }
                is ITyClass -> {
                    stream.writeName(ty.className)
                    stream.writeName(ty.varName)
                    stream.writeName(ty.superClassName)
                    stream.writeName(ty.aliasName)
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
                is TyTuple -> {
                    stream.writeByte(ty.list.size)
                    ty.list.forEach { serialize(it, stream) }
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
                    val arr = stream.readSignatures()
                    TySerializedFunction(mainSig, arr, flags)
                }
                TyKind.Class -> {
                    val className = stream.readName()
                    val varName = stream.readName()
                    val superName = stream.readName()
                    val aliasName = stream.readName()
                    createSerializedClass(StringRef.toString(className),
                            StringRef.toString(varName),
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
                TyKind.Nil -> NIL
                TyKind.Void -> VOID
                TyKind.Tuple -> {
                    val size = stream.readByte().toInt()
                    val list = mutableListOf<ITy>()
                    for (i in 0 until size) list.add(deserialize(stream))
                    TyTuple(list)
                }
                TyKind.GenericParam -> {
                    val name = StringRef.toString(stream.readName())
                    val base = StringRef.toString(stream.readName())
                    TyParameter(name, base)
                }
                else -> TyUnknown()
            }
        }
    }
}

interface ITyArray : ITy {
    val base: ITy
}

class TyArray(override val base: ITy) : Ty(TyKind.Array), ITyArray {

    override fun equals(other: Any?): Boolean {
        return other is ITyArray && base == other.base
    }

    override fun hashCode(): Int {
        return displayName.hashCode()
    }

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return super.subTypeOf(other, context, strict) || (other is TyArray && base.subTypeOf(other.base, context, strict)) || other == Ty.TABLE
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        return TyArray(base.substitute(substitutor))
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitArray(this)
    }

    override fun acceptChildren(visitor: ITyVisitor) {
        base.accept(visitor)
    }
}

class TyUnion : Ty(TyKind.Union) {
    private val childSet = mutableSetOf<ITy>()

    fun getChildTypes() = childSet

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

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return super.subTypeOf(other, context, strict) || childSet.any { type -> type.subTypeOf(other, context, strict) }
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        val u = TyUnion()
        childSet.forEach { u.childSet.add(it.substitute(substitutor)) }
        return u
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitUnion(this)
    }

    override fun acceptChildren(visitor: ITyVisitor) {
        childSet.forEach { it.accept(visitor) }
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
                // why nullable ???
                val arr: Array<ITy?> = ty.childSet.toTypedArray()
                for (child in arr) {
                    if (child != null && !process(child))
                        break
                }
            } else process(ty)
        }

        fun each(ty: ITy, fn: (ITy) -> Unit) {
            process(ty) {
                fn(it)
                true
            }
        }

        fun eachPerfect(ty: ITy, process: (ITy) -> Boolean) {
            if (ty is TyUnion) {
                val list = ty.childSet.sorted()
                for (iTy in list) {
                    if (!process(iTy))
                        break
                }
            } else process(ty)
        }

        fun union(t1: ITy, t2: ITy): ITy {
            return when {
                isInvalid(t1) -> t2
                isInvalid(t2) -> t1
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

        fun getPerfectClass(ty: ITy): ITyClass? {
            var clazz: ITyClass? = null
            var anonymous: ITyClass? = null
            var global: ITyClass? = null
            process(ty) {
                if (it is ITyClass) {
                    when {
                        it.isAnonymous -> anonymous = it
                        it.isGlobal -> global = it
                        else -> clazz = it
                    }
                }
                clazz == null
            }
            return clazz ?: global ?: anonymous
        }
    }
}

class TyUnknown : Ty(TyKind.Unknown) {

    override fun equals(other: Any?): Boolean {
        return other is TyUnknown
    }

    override fun hashCode(): Int {
        return Constants.WORD_ANY.hashCode()
    }

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return !strict
    }
}

class TyNil : Ty(TyKind.Nil) {

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {

        return super.subTypeOf(other, context, strict) || other is TyNil || !LuaSettings.instance.isNilStrict
    }
}

class TyVoid : Ty(TyKind.Void) {

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return false
    }
}

class TyTuple(val list: List<ITy>) : Ty(TyKind.Tuple) {

    val size: Int get() {
        return list.size
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        val list = list.map { it.substitute(substitutor) }
        return TyTuple(list)
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitTuple(this)
    }

    override fun acceptChildren(visitor: ITyVisitor) {
        list.forEach { it.accept(visitor) }
    }

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        if (other is TyTuple && other.size == size) {
            for (i in 0 until size) {
                if (!list[i].subTypeOf(other.list[i], context, strict)) {
                    return false
                }
            }
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        var hash = 0
        for (ty in list) {
            hash = hash * 31 + ty.hashCode()
        }
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other is TyTuple && other.size == size) {
            for (i in 0 until size) {
                if (list[i] != other.list[i]) {
                    return false
                }
            }
            return true
        }
        return super.equals(other)
    }
}