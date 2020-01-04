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


class TyStringLiteral private constructor(val content: String) : Ty(TyKind.StringLiteral) {
    companion object {
        private val stringLiterals = ConcurrentHashMap<String, TyStringLiteral>()

        fun getTy(content: String): TyStringLiteral {
            return stringLiterals.getOrPut(content, { TyStringLiteral(content) })
        }
    }

    override fun toString() = content

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        return other == STRING || super.subTypeOf(other, context, strict)
    }
}

object TyStringLiteralSerializer : TySerializer<TyStringLiteral>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): TyStringLiteral {
        return TyStringLiteral.getTy(stream.readUTF())
    }

    override fun serializeTy(ty: TyStringLiteral, stream: StubOutputStream) {
        stream.writeUTF(ty.content)
    }
}
