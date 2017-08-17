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
    Union
}

abstract class Ty(val kind: TyKind) {

    val isAnonymous: Boolean = false

    abstract val displayName: String

    fun union(ty: Ty): Ty {
        return TyUnion.union(this, ty)
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
        val list = mutableListOf<String>()
        TyUnion.each(this) {
            list.add(it.displayName)
        }
        return list.joinToString("|")
    }

    companion object {

        val UNKNOWN = TyUnknown()
        val BOOLEAN = TyPrimitive("b", "boolean")
        val STRING = TyPrimitive("s", "string")
        val NUMBER = TyPrimitive("n", "number")

        private fun getPrimitive(mark: String): Ty {
            return when (mark) {
                "b" -> BOOLEAN
                "s" -> STRING
                "n" -> NUMBER
                else -> UNKNOWN
            }
        }

        private fun getKind(ordinal: Int): TyKind {
            return TyKind.values().firstOrNull { ordinal == it.ordinal } ?: TyKind.Unknown
        }

        fun isInvalid(ty: Ty): Boolean {
            return ty is TyUnknown
        }

        fun serialize(ty: Ty, stream: StubOutputStream) {
            stream.writeByte(ty.kind.ordinal)
            when(ty) {
                is TyArray -> {
                    serialize(ty.base, stream)
                }
                is TyFunction -> {
                    stream.writeByte(ty.params.size)
                    for (param in ty.params) {
                        LuaParamInfo.serialize(param, stream)
                    }
                    serialize(ty.returnTy, stream)
                }
                is TyClass -> {
                    stream.writeName(ty.className)
                    stream.writeName(ty.superClassName)
                    stream.writeName(ty.aliasName)
                }
                is TyPrimitive -> {
                    stream.writeName(ty.name)
                }
                is TyUnion -> {
                    stream.writeByte(ty.size)
                    TyUnion.each(ty) { serialize(it, stream) }
                }
            }
        }

        fun deserialize(stream: StubInputStream): Ty {
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
                TyKind.Primitive -> {
                    val ref = stream.readName()
                    getPrimitive(StringRef.toString(ref))
                }
                TyKind.Union -> {
                    var union:Ty = TyUnion()
                    val size = stream.readByte()
                    for (i in 1 until size) {
                        union = TyUnion.union(union, deserialize(stream))
                    }
                    union
                }
                else -> TyUnknown()
            }
        }
    }
}

class TyPrimitive(val name: String, override val displayName: String) : Ty(TyKind.Primitive)

class TyArray(val base: Ty) : Ty(TyKind.Array) {
    override val displayName: String
        get() = "${base.displayName}[]"
}

class TyUnion : Ty(TyKind.Union) {
    private val children = mutableListOf<Ty>()

    override val displayName: String
        get() = "Union"

    val size:Int
        get() = children.size

    private fun union2(ty: Ty): TyUnion {
        if (ty is TyUnion)
            children.addAll(ty.children)
        else
            children.add(ty)
        return this
    }

    companion object {
        fun <T : Ty> find(ty: Ty, clazz: Class<T>): T? {
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

        fun process(ty: Ty, process: (Ty) -> Boolean) {
            if (ty is TyUnion) {
                for (child in ty.children) {
                    if (!process(child))
                        break
                }
            } else process(ty)
        }

        fun each(ty: Ty, process: (Ty) -> Unit) {
            if (ty is TyUnion) {
                ty.children.forEach(process)
            } else process(ty)
        }

        fun union(t1: Ty, t2: Ty): Ty {
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

        fun getPrefectClass(ty: Ty): TyClass? {
            var tc: TyClass? = null
            process(ty) {
                if (it is TyClass) {
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