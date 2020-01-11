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


class TySnippet(val content: String) : Ty(TyKind.Snippet) {
    override fun toString() = content
}

object TySnippetSerializer : TySerializer<TySnippet>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): TySnippet {
        return TySnippet(stream.readUTF())
    }

    override fun serializeTy(ty: TySnippet, stream: StubOutputStream) {
        stream.writeUTF(ty.content)
    }
}
