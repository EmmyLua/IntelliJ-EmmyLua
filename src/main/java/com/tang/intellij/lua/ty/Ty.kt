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
    Class
}

abstract class Ty(val kind: TyKind) {

    val isAnonymous: Boolean = false

    abstract val displayName: String

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

class TyUnknown : Ty(TyKind.Unknown) {
    override val displayName: String
        get() = "Unknown"
}