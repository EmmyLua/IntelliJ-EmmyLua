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

class TySet {

    val types: MutableList<Ty> = mutableListOf()

    val perfect: Ty? get() = perfectClass

    val perfectClass: TyClass? get() {
        types.forEach {
            if (it is TyClass) {
                return it
            }
        }
        return null
    }

    fun isEmpty() = types.isEmpty()

    fun union(set: TySet): TySet {
        val tySet = create()
        tySet.types.addAll(types)
        tySet.types.addAll(set.types)
        tySet.types.distinct()
        return tySet
    }

    fun createTypeString(): String? {
        val s = toString()
        return if (s.isEmpty()) "any" else s
    }

    fun createReturnString(): String? {
        val s = toString()
        return if (s.isEmpty()) "void" else s
    }

    override fun toString(): String {
        val set = types
                .map { it.displayName }
                .toTypedArray()
        return set.joinToString("|")
    }

    companion object {
        val EMPTY = TySet()

        fun create(vararg tys: Ty): TySet {
            val tySet = TySet()
            tySet.types.addAll(tys)
            return tySet
        }

        fun union(set: TySet, ty: Ty): TySet {
            return create(ty).union(set)
        }

        fun serialize(set: TySet, stream: StubOutputStream) {
            stream.writeByte(set.types.size)
            for (ty in set.types) {
                Ty.serialize(ty, stream)
            }
        }

        fun deserialize(stream: StubInputStream): TySet {
            val len = stream.readByte()
            val set = create()
            for (i in 0 until len) {
                set.types.add(Ty.deserialize(stream))
            }
            return set
        }
    }
}