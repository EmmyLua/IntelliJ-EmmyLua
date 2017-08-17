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
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.psi.LuaClassField
import com.tang.intellij.lua.psi.LuaClassMethod
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import com.tang.intellij.lua.psi.LuaParamInfo
import com.tang.intellij.lua.search.SearchContext

enum class TyKind {
    Unknown,
    Primitive,
    Array,
    Function,
    Class
}

class TySet {

}

abstract class Ty(val kind: TyKind) {

    companion object {
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
                    stream.writeName(ty.name)
                }
            }
        }

        fun deserialize(stream: StubInputStream): Ty {
            val kind = getKind(stream.readByte().toInt())
            val ty = when (kind) {
                TyKind.Array -> {
                    val base = deserialize(stream)
                    TyArray(base)
                }
                TyKind.Function -> {
                    val size = stream.readByte()
                    val arr = mutableListOf<LuaParamInfo>()
                    for (i in 0 .. size) {
                        arr.add(LuaParamInfo.deserialize(stream))
                    }
                    val retTy = deserialize(stream)
                    TySerializedFunction(retTy, arr.toTypedArray())
                }
                TyKind.Class -> {
                    val ref = stream.readName()
                    TySerializedClass(StringRef.toString(ref))
                }
                else -> TyUnknown()
            }
            return ty
        }

        val UNKNOWN = TyUnknown()
    }
}

abstract class TyPrimitive : Ty(TyKind.Primitive) {
    companion object {

    }
}

class TyArray(val base: Ty) : Ty(TyKind.Array)

abstract class TyFunction : Ty(TyKind.Function) {
    abstract val returnTy: Ty
    abstract val params: Array<LuaParamInfo>
}

class TyPsiFunction(val psi: LuaFuncBodyOwner) : TyFunction() {
    override val returnTy: Ty
        get() = UNKNOWN
    override val params: Array<LuaParamInfo>
        get() = psi.params
}

class TySerializedFunction(override val returnTy: Ty, override val params: Array<LuaParamInfo>) : TyFunction()

abstract class TyClass(val name: String) : Ty(TyKind.Class) {
    open fun processFields(searchContext: SearchContext, processor: (ty:Ty, t:LuaClassField) -> Unit) {}
    open fun processMethods(searchContext: SearchContext, processor: (ty:Ty, t:LuaClassMethod) -> Unit) {}
}

class TyPsiDocClass(val dc: LuaDocClassDef) : TyClass(dc.name) {

}

class TySerializedClass(name: String) : TyClass(name)

class TyUnknown : Ty(TyKind.Unknown)