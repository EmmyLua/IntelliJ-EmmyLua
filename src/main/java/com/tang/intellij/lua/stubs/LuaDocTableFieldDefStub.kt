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
import com.tang.intellij.lua.comment.psi.LuaDocTableField
import com.tang.intellij.lua.comment.psi.impl.LuaDocTableFieldImpl
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.psi.Visibility
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.getDocTableTypeName

class LuaDocTableFieldDefType : LuaStubElementType<LuaDocTableFieldDefStub, LuaDocTableField>("DOC_TABLE_FIELD_DEF") {
    override fun createPsi(stub: LuaDocTableFieldDefStub): LuaDocTableField {
        return LuaDocTableFieldImpl(stub, this)
    }

    override fun serialize(stub: LuaDocTableFieldDefStub, stream: StubOutputStream) {
        stream.writeName(stub.name)
        stream.writeTyNullable(stub.docTy)
        stream.writeName(stub.parentTypeName)
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): LuaDocTableFieldDefStub {
        val name = stream.readName()
        val docTy = stream.readTyNullable()
        val parentTypeName = stream.readName()
        return LuaDocTableFieldDefStubImpl(StringRef.toString(name),
                docTy,
                StringRef.toString(parentTypeName),
                parent)
    }

    override fun createStub(tableDef: LuaDocTableField, parentStub: StubElement<*>): LuaDocTableFieldDefStub {
        val name = tableDef.name
        val type = tableDef.ty?.getType()
        val p = tableDef.parent as LuaDocTableDef
        val pTypeName = getDocTableTypeName(p)
        return LuaDocTableFieldDefStubImpl(name, type, pTypeName, parentStub)
    }

    override fun indexStub(stub: LuaDocTableFieldDefStub, sink: IndexSink) {
        LuaClassMemberIndex.indexStub(sink, stub.parentTypeName, stub.name)
    }
}

interface LuaDocTableFieldDefStub : LuaClassMemberStub<LuaDocTableField> {
    val name: String
    val parentTypeName: String
}

class LuaDocTableFieldDefStubImpl(
        override val name: String,
        override val docTy: ITy?,
        override val parentTypeName: String,
        parent: StubElement<*>
) : LuaDocStubBase<LuaDocTableField>(parent, LuaElementType.DOC_TABLE_FIELD_DEF), LuaDocTableFieldDefStub {
    override val visibility: Visibility
        get() = Visibility.PUBLIC
    override val isDeprecated: Boolean
        get() = false
}