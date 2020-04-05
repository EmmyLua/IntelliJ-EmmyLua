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
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.getDocTableTypeName

class LuaDocTableFieldType : LuaStubElementType<LuaDocTableFieldStub, LuaDocTableField>("DOC_TABLE_FIELD_DEF") {
    override fun createPsi(stub: LuaDocTableFieldStub): LuaDocTableField {
        return LuaDocTableFieldImpl(stub, this)
    }

    override fun serialize(stub: LuaDocTableFieldStub, stream: StubOutputStream) {
        stream.writeName(stub.name)
        stream.writeTyNullable(stub.indexTy)
        stream.writeName(stub.parentTypeName)
        stream.writeTyNullable(stub.valueTy)
    }

    override fun deserialize(stream: StubInputStream, stubElement: StubElement<*>): LuaDocTableFieldStub {
        val name = StringRef.toString(stream.readName())
        val indexType = stream.readTyNullable()
        val parentTypeName = StringRef.toString(stream.readName())!!
        val valueType = stream.readTyNullable()

        return if (name != null) {
            LuaDocTableFieldStubImpl(stubElement,
                    name,
                    parentTypeName,
                    valueType)
        } else {
            LuaDocTableFieldStubImpl(stubElement,
                    indexType!!,
                    parentTypeName,
                    valueType)
        }
    }

    override fun createStub(tableDef: LuaDocTableField, parentStub: StubElement<*>): LuaDocTableFieldStub {
        val name = tableDef.name
        val indexTy = tableDef.indexType?.getType()
        val valueTy = tableDef.valueType?.getType()
        val parent = tableDef.parent as LuaDocTableDef
        val parentTypeName = getDocTableTypeName(parent)

        return if (name != null) {
            LuaDocTableFieldStubImpl(parentStub,
                    name,
                    parentTypeName,
                    valueTy)
        } else {
            LuaDocTableFieldStubImpl(parentStub,
                    indexTy!!,
                    parentTypeName,
                    valueTy)
        }
    }

    override fun indexStub(stub: LuaDocTableFieldStub, sink: IndexSink) {
        val memberName = stub.name
        val parentTypeName = stub.parentTypeName

        if (memberName != null) {
            LuaClassMemberIndex.indexMemberStub(sink, parentTypeName, memberName)
            sink.occurrence(StubKeys.SHORT_NAME, memberName)
            return
        }

        LuaClassMemberIndex.indexIndexerStub(sink, parentTypeName, stub.indexTy!!)
    }
}

interface LuaDocTableFieldStub : LuaClassMemberStub<LuaDocTableField> {
    val name: String?
    val indexTy: ITy?
    val valueTy: ITy?
    val parentTypeName: String

    override val docTy: ITy?
        get() = valueTy

    override val visibility: Visibility
        get() = Visibility.PUBLIC

    override val isDeprecated: Boolean
        get() = false
}

class LuaDocTableFieldStubImpl : LuaDocStubBase<LuaDocTableField>, LuaDocTableFieldStub {
    override val name: String?
    override val indexTy: ITy?
    override val valueTy: ITy?
    override val parentTypeName: String

    constructor(parent: StubElement<*>, name: String, parentTypeName: String, valueTy: ITy?)
            : super(parent, LuaElementType.DOC_TABLE_FIELD_DEF) {
        this.name = name
        this.indexTy = null
        this.parentTypeName = parentTypeName
        this.valueTy = valueTy
    }

    constructor(parent: StubElement<*>, indexType: ITy, parentTypeName: String, valueTy: ITy?)
            : super(parent, LuaElementType.DOC_TABLE_FIELD_DEF) {
        this.name = null
        this.indexTy = indexType
        this.parentTypeName = parentTypeName
        this.valueTy = valueTy
    }
}
