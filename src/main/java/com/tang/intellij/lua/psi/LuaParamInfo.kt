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

package com.tang.intellij.lua.psi

import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.BitUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.ITyClass
import com.tang.intellij.lua.ty.ITySubstitutor
import com.tang.intellij.lua.ty.Ty

/**
 * parameter info
 * Created by tangzx on 2017/2/4.
 */
class LuaParamInfo {

    var flags: Byte = 0
    var name: String = ""
    var ty: ITy = Ty.UNKNOWN

    constructor(name: String, ty: ITy) {
        this.name = name
        this.ty = ty
    }

    constructor()

    override fun equals(other: Any?): Boolean {
        //only check ty
        return other is LuaParamInfo && other.ty == ty
    }

    override fun hashCode(): Int {
        return ty.hashCode()
    }

    var isVarArgs: Boolean
        get() = BitUtil.isSet(flags, FLAG_VARARGS)
        set(value) {
            flags = BitUtil.set(flags, FLAG_VARARGS, value)
            if (value) name = "..."
        }

    var isSelf: Boolean
        get() = BitUtil.isSet(flags, FLAG_SELF)
        set(value) {
            flags = BitUtil.set(flags, FLAG_SELF, value)
            if (value) name = Constants.WORD_SELF
        }

    fun substitute(substitutor: ITySubstitutor): LuaParamInfo {
        val pi = LuaParamInfo()
        pi.flags = flags
        pi.name = name
        pi.ty = ty.substitute(substitutor)
        return pi
    }

    companion object {
        const val FLAG_VARARGS: Byte = 0x1
        const val FLAG_SELF: Byte = 0x2

        fun createSelf(thisType: ITy? = null): LuaParamInfo {
            val pi = LuaParamInfo(Constants.WORD_SELF, thisType ?: Ty.UNKNOWN)
            pi.isSelf = true
            return pi
        }

        fun deserialize(stubInputStream: StubInputStream): LuaParamInfo {
            val paramInfo = LuaParamInfo()
            paramInfo.name = StringRef.toString(stubInputStream.readName())
            paramInfo.flags = stubInputStream.readByte()
            paramInfo.ty = Ty.deserialize(stubInputStream)
            return paramInfo
        }

        fun serialize(param: LuaParamInfo, stubOutputStream: StubOutputStream) {
            stubOutputStream.writeName(param.name)
            stubOutputStream.writeByte(param.flags.toInt())
            Ty.serialize(param.ty, stubOutputStream)
        }
    }
}