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
import com.intellij.util.BitUtil
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.LuaDocClassRef
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.search.SearchContext

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
    PrimitiveLiteral,
    Snippet
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
class TyVarianceFlags {
    companion object {
        const val STRICT_UNKNOWN = 0x1
        const val ABSTRACT_PARAMS = 0x2
    }
}

interface ITy : Comparable<ITy> {
    val kind: TyKind

    val displayName: String

    val flags: Int

    val booleanType: ITy

    fun union(ty: ITy): ITy

    fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean

    fun covariantOf(other: ITy, context: SearchContext, flags: Int): Boolean

    fun getSuperClass(context: SearchContext): ITy?

    fun getParams(context: SearchContext): Array<TyParameter>?

    fun visitSuper(searchContext: SearchContext, processor: Processor<ITyClass>)

    fun substitute(substitutor: ITySubstitutor): ITy

    fun each(fn: (ITy) -> Unit) {
        TyUnion.each(this, fn)
    }

    fun eachTopClass(fn: Processor<ITy>)

    fun accept(visitor: ITyVisitor)

    fun acceptChildren(visitor: ITyVisitor)

    fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Unit, deep: Boolean = true)

    fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Unit) {
        processMembers(context, processor, true)
    }
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

    override val booleanType: ITy = Ty.TRUE

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

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        if (this == other
                || (other.kind == TyKind.Unknown && flags and TyVarianceFlags.STRICT_UNKNOWN == 0)
                || (other.kind == TyKind.Nil && !LuaSettings.instance.isNilStrict)) {
            return true
        }

        if (other is TyUnion || other == Ty.BOOLEAN) {
            var contravariant = true
            TyUnion.process(other, {
                contravariant = contravariantOf(it, context, flags)
                contravariant
            })
            return contravariant
        }

        val otherSuper = other.getSuperClass(context)
        return otherSuper != null && contravariantOf(otherSuper, context, flags)
    }

    override fun covariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        return other.contravariantOf(this, context, flags)
    }

    override fun getSuperClass(context: SearchContext): ITy? {
        return null
    }

    override fun getParams(context: SearchContext): Array<TyParameter>? {
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

    override fun eachTopClass(fn: Processor<ITy>) {
        when (this) {
            is ITyClass -> fn.process(this)
            is ITyGeneric -> fn.process(this)
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

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Unit, deep: Boolean) {
    }

    companion object {

        val UNKNOWN = TyUnknown()
        val VOID = TyVoid()
        val BOOLEAN = TyPrimitive(TyPrimitiveKind.Boolean, Constants.WORD_BOOLEAN)
        val TRUE = TyPrimitiveLiteral.getTy(TyPrimitiveKind.Boolean, Constants.WORD_TRUE)
        val FALSE = TyPrimitiveLiteral.getTy(TyPrimitiveKind.Boolean, Constants.WORD_FALSE)
        val STRING = TyPrimitiveClass(TyPrimitiveKind.String, Constants.WORD_STRING)
        val NUMBER = TyPrimitive(TyPrimitiveKind.Number, Constants.WORD_NUMBER)
        val TABLE = TyPrimitive(TyPrimitiveKind.Table, Constants.WORD_TABLE)
        val FUNCTION = TyPrimitive(TyPrimitiveKind.Function, Constants.WORD_FUNCTION)
        val NIL = TyNil()

        private val serializerMap = mapOf<TyKind, ITySerializer>(
                TyKind.Array to TyArraySerializer,
                TyKind.Class to TyClassSerializer,
                TyKind.Function to TyFunctionSerializer,
                TyKind.Generic to TyGenericSerializer,
                TyKind.GenericParam to TyGenericParamSerializer,
                TyKind.Primitive to TyPrimitiveSerializer,
                TyKind.PrimitiveLiteral to TyPrimitiveLiteralSerializer,
                TyKind.Snippet to TySnippetSerializer,
                TyKind.Tuple to TyTupleSerializer,
                TyKind.Union to TyUnionSerializer
        )

        private fun getKind(ordinal: Int): TyKind {
            return TyKind.values().firstOrNull { ordinal == it.ordinal } ?: TyKind.Unknown
        }

        fun getBuiltin(name: String): ITy? {
            return when (name) {
                Constants.WORD_NIL -> NIL
                Constants.WORD_VOID -> VOID
                Constants.WORD_ANY -> UNKNOWN
                Constants.WORD_BOOLEAN -> BOOLEAN
                Constants.WORD_TRUE -> TRUE
                Constants.WORD_FALSE -> FALSE
                Constants.WORD_STRING -> STRING
                Constants.WORD_NUMBER -> NUMBER
                Constants.WORD_TABLE -> TABLE
                Constants.WORD_FUNCTION -> FUNCTION
                else -> null
            }
        }

        fun create(name: String): ITy {
            return getBuiltin(name) ?: TyLazyClass(name)
        }

        fun create(classRef: LuaDocClassRef): ITy {
            val simpleType = Ty.create(classRef.classNameRef.id.text)
            return if (classRef.tyList.size > 0) {
                val params = simpleType.getParams(SearchContext.get(classRef))
                val args = linkedMapOf<String, ITy>()
                TySerializedGeneric(classRef.tyList.map { it.getType() }.toTypedArray(), simpleType)
            } else simpleType
        }

        fun isInvalid(ty: ITy?): Boolean {
            return ty == null || ty is TyVoid
        }

        private fun getSerializer(kind: TyKind): ITySerializer? {
            return serializerMap[kind]
        }

        fun serialize(ty: ITy, stream: StubOutputStream) {
            stream.writeByte(ty.kind.ordinal)
            stream.writeInt(ty.flags)
            val serializer = getSerializer(ty.kind)
            serializer?.serialize(ty, stream)
        }

        fun deserialize(stream: StubInputStream): ITy {
            val kind = getKind(stream.readByte().toInt())
            val flags = stream.readInt()
            return when (kind) {
                TyKind.Nil -> NIL
                TyKind.Void -> VOID
                else -> {
                    val serializer = getSerializer(kind)
                    serializer?.deserialize(flags, stream) ?: UNKNOWN
                }
            }
        }

        fun processSuperClass(start: ITy, searchContext: SearchContext, processor: (ITy) -> Boolean): Boolean {
            val processedName = mutableSetOf<String>()
            var cur: ITy? = start
            while (cur != null) {
                val superType = cur.getSuperClass(searchContext)
                if (superType != null) {
                    if (!processedName.add(superType.displayName)) {
                        // todo: Infinite inheritance
                        return false
                    }
                    if (!processor(superType))
                        return false
                }
                cur = superType
            }
            return true
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

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        return true
    }
}

class TyNil : Ty(TyKind.Nil) {

    override val booleanType = Ty.FALSE

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        return other.kind == TyKind.Nil
    }
}

class TyVoid : Ty(TyKind.Void) {

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        return false
    }
}
