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
import java.util.concurrent.ConcurrentHashMap


class TyPrimitiveLiteral private constructor(override val primitiveKind: TyPrimitiveKind, val value: String) : Ty(TyKind.PrimitiveLiteral), ITyPrimitive {
    companion object {
        private val primitiveLiterals = ConcurrentHashMap<String, TyPrimitiveLiteral>()

        fun getTy(primitiveKind: TyPrimitiveKind, value: String): TyPrimitiveLiteral {
            val id = "$primitiveKind:$value"
            return primitiveLiterals.getOrPut(id, { TyPrimitiveLiteral(primitiveKind, value) })
        }
    }

    override val displayName: String by lazy { if (primitiveKind == TyPrimitiveKind.String) "\"${value.replace("\"", "\\\"")}\"" else value }

    // Ty.TRUE/Ty.FALSE are TyPrimitiveLiteral, to avoid circular references (a null booleanType) we make this lazy.
    override val booleanType by lazy { if (primitiveKind != TyPrimitiveKind.Boolean || value == "true") Ty.TRUE else Ty.FALSE }

    val primitiveType = when (primitiveKind) {
        TyPrimitiveKind.Boolean -> Ty.BOOLEAN
        TyPrimitiveKind.Function -> Ty.FUNCTION
        TyPrimitiveKind.Number -> Ty.NUMBER
        TyPrimitiveKind.String -> Ty.STRING
        TyPrimitiveKind.Table -> Ty.TABLE
    }

    override fun getSuperClass(context: SearchContext): ITy? {
        return primitiveType
    }

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        // Even when !LuaSettings.instance.isNilStrict, nil is never assignable to a primitive literal.
        return this == other || (other is TyUnknown && flags and TyVarianceFlags.STRICT_UNKNOWN == 0)
    }

    override fun equals(other: Any?): Boolean {
        return other is TyPrimitiveLiteral && primitiveKind == other.primitiveKind && value.equals(other.value)
    }

    override fun hashCode(): Int {
        return primitiveKind.hashCode() * 31 * value.hashCode()
    }
}

object TyPrimitiveLiteralSerializer : TySerializer<TyPrimitiveLiteral>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): TyPrimitiveLiteral {
        val primitiveKind = stream.readByte().toInt()
        return TyPrimitiveLiteral.getTy(TyPrimitiveKind.values()[primitiveKind], stream.readUTF())
    }

    override fun serializeTy(ty: TyPrimitiveLiteral, stream: StubOutputStream) {
        stream.writeByte(ty.primitiveKind.ordinal)
        stream.writeUTF(ty.value)
    }
}
