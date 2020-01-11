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
        return childSet.add(ty)
    }

    override fun covariantWith(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return super.covariantWith(other, context, strict)
                || childSet.any { type -> type.covariantWith(other, context, strict) }
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        val u = TyUnion()
        childSet.forEach { u.append(it.substitute(substitutor)) }
        return u
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
            } else if (ty == Ty.BOOLEAN) {
                if (process(Ty.TRUE)) {
                    process(Ty.FALSE)
                }
            } else process(ty)
        }

        fun each(ty: ITy, fn: (ITy) -> Unit) {
            process(ty) {
                fn(it)
                true
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
                isInvalid(t1) -> t2
                isInvalid(t2) -> t1
                t1 is TyUnion -> t1.append(t2)
                t2 is TyUnion -> t2.append(t1)
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
