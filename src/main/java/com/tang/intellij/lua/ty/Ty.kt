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
    Alias,
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
        const val SHAPE = 0x10 // variance is considered per field
    }
}
class TyVarianceFlags {
    companion object {
        const val STRICT_UNKNOWN = 0x1
        const val ABSTRACT_PARAMS = 0x2 // A generic is to be considered contravariant if its TyParameter generic parameters are contravariant.
        const val WIDEN_TABLES = 0x4 // A generic table is to be considered contravariant if its generic parameters are contravariant.
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

    fun findMember(name: String, searchContext: SearchContext): LuaClassMember?
    fun findIndexer(indexTy: ITy, searchContext: SearchContext): LuaClassMember?

    fun guessMemberType(name: String, searchContext: SearchContext): ITy? {
        return findMember(name, searchContext)?.guessType(searchContext)?.let {
            val substitutor = getMemberSubstitutor(searchContext)
            return if (substitutor != null) it.substitute(substitutor) else it
        }
    }

    fun guessIndexerType(indexTy: ITy, searchContext: SearchContext): ITy? {
        return findIndexer(indexTy, searchContext)?.guessType(searchContext)?.let {
            val substitutor = getMemberSubstitutor(searchContext)
            return if (substitutor != null) it.substitute(substitutor) else it
        }
    }

    fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean, deep: Boolean = true): Boolean

    fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean): Boolean {
        return processMembers(context, processor, true)
    }

    fun findSuperMember(name: String, searchContext: SearchContext): LuaClassMember? {
        // Travel up the hierarchy to find the lowest member of this type on a superclass (excluding this class)
        var member: LuaClassMember? = null
        Ty.processSuperClass(this, searchContext) { superType ->
            val superClass = (if (superType is ITyGeneric) superType.base else superType) as? ITyClass
            member = superClass?.findMember(name, searchContext)
            member == null
        }
        return member
    }

    fun getMemberSubstitutor(context: SearchContext): ITySubstitutor? {
        return getSuperClass(context)?.getMemberSubstitutor(context)
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

        val resolvedOther = TyAliasSubstitutor.substitute(other, context)

        if (resolvedOther != other) {
            return contravariantOf(resolvedOther, context, flags)
        }

        if (other is TyUnion) {
            TyUnion.each(other) {
                if (!contravariantOf(it, context, flags)) {
                    return false
                }
            }

            return true
        }

        if (this.flags and TyFlags.SHAPE != 0) {
            return processMembers(context, { _, classMember ->
                val indexTy = classMember.indexType?.getType()

                val memberTy = if (indexTy != null) {
                    guessIndexerType(indexTy, context)
                } else {
                    classMember.name?.let { guessMemberType(it, context) }
                } ?: Ty.UNKNOWN

                val otherMemberTy = if (indexTy != null) {
                    other.guessIndexerType(indexTy, context)
                } else {
                    classMember.name?.let { other.guessMemberType(it, context) }
                }

                if (otherMemberTy == null) {
                    return@processMembers TyUnion.find(memberTy, TyNil::class.java) != null
                }

                memberTy.contravariantOf(otherMemberTy, context, flags)
            }, true)
        } else {
            val otherSuper = other.getSuperClass(context)
            return otherSuper != null && contravariantOf(otherSuper, context, flags)
        }
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
            is TyMultipleResults -> {
                list.firstOrNull()?.eachTopClass(fn)
            }
        }
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return null
    }

    override fun findIndexer(indexTy: ITy, searchContext: SearchContext): LuaClassMember? {
        return null
    }

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean, deep: Boolean): Boolean {
        return true
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
                TyKind.Alias to TyAliasSerializer,
                TyKind.Function to TyFunctionSerializer,
                TyKind.Generic to TyGenericSerializer,
                TyKind.GenericParam to TyGenericParamSerializer,
                TyKind.Primitive to TyPrimitiveSerializer,
                TyKind.PrimitiveLiteral to TyPrimitiveLiteralSerializer,
                TyKind.Snippet to TySnippetSerializer,
                TyKind.Tuple to TyMultipleResultsSerializer,
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
                        return true
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

    override fun guessMemberType(name: String, searchContext: SearchContext): ITy? {
        return if (LuaSettings.instance.isUnknownIndexable) Ty.UNKNOWN else null
    }

    override fun guessIndexerType(indexTy: ITy, searchContext: SearchContext): ITy? {
        return if (LuaSettings.instance.isUnknownIndexable) Ty.UNKNOWN else null
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
        return other.kind == TyKind.Void
    }
}
