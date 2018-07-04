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

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaLiteralExprImpl

class LuaLiteralElementType
    : LuaStubElementType<LuaLiteralExprStub, LuaLiteralExpr>("LITERAL_EXPR") {

    override fun shouldCreateStub(node: ASTNode): Boolean {
        return createStubIfParentIsStub(node)
    }

    override fun createStub(expr: LuaLiteralExpr, parentStub: StubElement<*>?): LuaLiteralExprStub {
        val str = if (expr.kind == LuaLiteralKind.String) expr.stringValue else null
        return LuaLiteralExprStub(expr.kind, expr.tooLargerString, str, parentStub, this)
    }

    override fun serialize(stub: LuaLiteralExprStub, stream: StubOutputStream) {
        stream.writeByte(stub.kind.ordinal)
        val str = stub.string
        stream.writeBoolean(stub.tooLargerString)
        val writeStr = str != null && !stub.tooLargerString
        stream.writeBoolean(writeStr)
        if (writeStr)
            stream.writeUTF(str)
    }

    override fun deserialize(stream: StubInputStream, parentStub: StubElement<*>?): LuaLiteralExprStub {
        val kind = stream.readByte()
        val tooLargerString = stream.readBoolean()
        val hasStr = stream.readBoolean()
        val str = if (hasStr) stream.readUTF() else null
        return LuaLiteralExprStub(LuaLiteralKind.toEnum(kind), tooLargerString, str, parentStub, this)
    }

    override fun indexStub(stub: LuaLiteralExprStub, sink: IndexSink) {

    }

    override fun createPsi(stub: LuaLiteralExprStub): LuaLiteralExpr {
        return LuaLiteralExprImpl(stub, this)
    }
}

class LuaLiteralExprStub(
        val kind: LuaLiteralKind,
        val tooLargerString: Boolean,
        val string: String?,
        parent: StubElement<*>?,
        type: LuaStubElementType<*, *>
) : LuaExprStubImpl<LuaLiteralExpr>(parent, type)