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
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.search.SearchContext

class TyUnion : Ty(TyKind.Union) {
    private val childSet = mutableSetOf<ITy>()

    fun getChildTypes() = childSet

    val size:Int
        get() = childSet.size

    override val booleanType: ITy
        get() {
            var resolvedType: ITy? = null
            childSet.forEach {
                when (it.booleanType) {
                    Ty.TRUE -> {
                        if (resolvedType == Ty.FALSE) return Ty.BOOLEAN
                        resolvedType = Ty.TRUE
                    }
                    Ty.FALSE -> {
                        if (resolvedType == Ty.TRUE) return Ty.BOOLEAN
                        resolvedType = Ty.FALSE
                    }
                    else -> return Ty.BOOLEAN
                }
            }
            return resolvedType ?: Ty.BOOLEAN
        }

    fun append(ty: ITy): TyUnion {
        if (ty is TyUnion) {
            ty.childSet.forEach { addChild(it) }
        }
        else addChild(ty)
        return this
    }

    private fun addChild(ty: ITy): Boolean {
        if (ty == Ty.FALSE || ty == Ty.TRUE) {
            if (childSet.contains(Ty.BOOLEAN)) {
                return false
            }

            if (ty == Ty.FALSE) {
                if (childSet.contains(Ty.TRUE)) {
                    childSet.remove(Ty.TRUE)
                    return childSet.add(Ty.BOOLEAN)
                }
            } else if (ty == Ty.TRUE) {
                if (childSet.contains(Ty.FALSE)) {
                    childSet.remove(Ty.FALSE)
                    return childSet.add(Ty.BOOLEAN)
                }
            }
        } else if (ty == Ty.BOOLEAN) {
            childSet.remove(Ty.TRUE)
            childSet.remove(Ty.FALSE)
        }

        return childSet.add(ty)
    }

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        return super.contravariantOf(other, context, flags)
                || childSet.any { type -> type.contravariantOf(other, context, flags) }
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        var ty: ITy = Ty.VOID
        childSet.forEach { ty = ty.union(it.substitute(substitutor)) }
        return ty
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return childSet.firstOrNull()?.findMember(name, searchContext)?.let { member ->
            childSet.asSequence().drop(1).forEach {
                if (it.findMember(name, searchContext) != member) {
                    return null
                }
            }
            member
        }
    }

    override fun findIndexer(indexTy: ITy, searchContext: SearchContext): LuaClassMember? {
        return childSet.firstOrNull()?.findIndexer(indexTy, searchContext)?.let { member ->
            childSet.asSequence().drop(1).forEach {
                if (it.findIndexer(indexTy, searchContext) != member) {
                    return null
                }
            }
            member
        }
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitUnion(this)
    }

    override fun acceptChildren(visitor: ITyVisitor) {
        childSet.forEach { it.accept(visitor) }
    }

    override fun equals(other: Any?): Boolean {
        return other is TyUnion && other.hashCode() == hashCode()
    }

    override fun hashCode(): Int {
        var code = 0
        childSet.forEach { code = code * 31 + it.hashCode() }
        return code
    }

    companion object {
        fun <T : ITy> find(ty: ITy, clazz: Class<T>): T? {
            if (clazz.isInstance(ty))
                return clazz.cast(ty)
            each(ty) {
                if (clazz.isInstance(it)) {
                    return clazz.cast(it)
                }
            }
            return null
        }

        inline fun each(ty: ITy, fn: (ITy) -> Unit) {
            if (ty is TyUnion) {
                for (child in ty.getChildTypes().toTypedArray()) {
                    fn(child)
                }
            } else if (ty == Ty.BOOLEAN) {
                fn(Ty.TRUE)
                fn(Ty.FALSE)
            } else {
                fn(ty)
            }
        }

        // used by ver.2017
        @Suppress("unused")
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
                t1 == t2 -> t1
                t1 is TyUnknown || t2 is TyUnknown -> Ty.UNKNOWN
                isInvalid(t1) -> t2
                isInvalid(t2) -> t1
                t1 is TyUnion -> t1.append(t2)
                t2 is TyUnion -> t2.append(t1)
                else -> {
                    val u = TyUnion()
                    u.addChild(t1)
                    u.addChild(t2)
                    //if t1 == t2
                    if (u.childSet.size == 1) u.childSet.first() else u
                }
            }
        }

        fun getPerfectClass(ty: ITy): ITyClass? {
            var clazz: ITyClass? = null
            var anonymous: ITyClass? = null
            var global: ITyClass? = null
            each(ty) {
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

object TyUnionSerializer : TySerializer<TyUnion>() {
    override fun serializeTy(ty: TyUnion, stream: StubOutputStream) {
        stream.writeInt(ty.size)
        TyUnion.each(ty) { Ty.serialize(it, stream) }
    }

    override fun deserializeTy(flags: Int, stream: StubInputStream): TyUnion {
        val union = TyUnion()
        val size = stream.readInt()
        for (i in 0 until size) {
            union.append(Ty.deserialize(stream))
        }
        return union
    }
}
