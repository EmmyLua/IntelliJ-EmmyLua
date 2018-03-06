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

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.comment.psi.LuaDocTableDef
import com.tang.intellij.lua.comment.psi.impl.LuaDocTableDefImpl
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.ty.getDocTableTypeName

class LuaDocTableDefType : LuaStubElementType<LuaDocTableDefStub, LuaDocTableDef>("DOC_TABLE_DEF") {
    override fun createPsi(stub: LuaDocTableDefStub): LuaDocTableDef {
        return LuaDocTableDefImpl(stub, this)
    }

    override fun serialize(stub: LuaDocTableDefStub, stream: StubOutputStream) {
        stream.writeName(stub.className)
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): LuaDocTableDefStub {
        val name = StringRef.toString(stream.readName())
        return LuaDocTableDefStubImpl(name, parent)
    }

    override fun createStub(tableDef: LuaDocTableDef, parentStub: StubElement<*>): LuaDocTableDefStub {
        return LuaDocTableDefStubImpl(getDocTableTypeName(tableDef), parentStub)
    }

    override fun indexStub(stub: LuaDocTableDefStub, sink: IndexSink) {
        //sink.occurrence(StubKeys.CLASS, stub.className)
    }
}

interface LuaDocTableDefStub : StubElement<LuaDocTableDef> {
    val className: String
}

class LuaDocTableDefStubImpl(
        override val className: String,
        parent: StubElement<*>
) : LuaDocStubBase<LuaDocTableDef>(parent, LuaElementType.DOC_TABLE_DEF), LuaDocTableDefStub {

}