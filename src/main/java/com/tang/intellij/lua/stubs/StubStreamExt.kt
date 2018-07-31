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

package com.tang.intellij.lua.stubs

import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.psi.LuaParamInfo
import com.tang.intellij.lua.ty.*

fun StubOutputStream.writeParamInfoArray(params: Array<LuaParamInfo>) {
    writeByte(params.size)
    for (param in params) {
        LuaParamInfo.serialize(param, this)
    }
}

fun StubInputStream.readParamInfoArray(): Array<LuaParamInfo> {
    val list = mutableListOf<LuaParamInfo>()
    val size = readByte()
    for (j in 0 until size) {
        list.add(LuaParamInfo.deserialize(this))
    }
    return list.toTypedArray()
}

fun StubOutputStream.writeSignatures(signatures: Array<IFunSignature>) {
    writeByte(signatures.size)
    for (sig in signatures) {
        FunSignature.serialize(sig, this)
    }
}

fun StubInputStream.readSignatures(): Array<IFunSignature> {
    val size = readByte()
    val arr = mutableListOf<IFunSignature>()
    for (i in 0 until size) {
        arr.add(FunSignature.deserialize(this))
    }
    return arr.toTypedArray()
}

fun StubInputStream.readTyNullable(): ITy? {
    val notNull = readBoolean()
    return if (notNull) Ty.deserialize(this) else null
}

fun StubOutputStream.writeTyNullable(ty: ITy?) {
    writeBoolean(ty != null)
    if (ty != null)
        Ty.serialize(ty, this)
}

fun StubOutputStream.writeNames(names: Array<String>) {
    writeInt(names.size)
    names.forEach { writeName(it) }
}

fun StubInputStream.readNames(): Array<String> {
    val list = mutableListOf<String>()
    val size = readInt()
    for (i in 0 until size) {
        list.add(StringRef.toString(readName()))
    }
    return list.toTypedArray()
}

fun StubOutputStream.writeTyParams(tyParams: Array<TyParameter>) {
    writeByte(tyParams.size)
    tyParams.forEach { parameter ->
        writeName(parameter.name)
        writeName(parameter.superClassName)
    }
}

fun StubInputStream.readTyParams(): Array<TyParameter> {
    val list = mutableListOf<TyParameter>()
    val size = readByte()
    for (i in 0 until size) {
        val name = StringRef.toString(readName())
        val base = StringRef.toString(readName())
        list.add(TyParameter(name, base))
    }
    return list.toTypedArray()
}