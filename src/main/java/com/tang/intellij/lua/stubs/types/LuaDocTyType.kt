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

package com.tang.intellij.lua.stubs.types

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.tang.intellij.lua.comment.psi.LuaDocTypeDef
import com.tang.intellij.lua.comment.psi.impl.LuaDocTypeDefImpl
import com.tang.intellij.lua.stubs.LuaDocTyStub
import com.tang.intellij.lua.stubs.LuaDocTyStubImpl
import com.tang.intellij.lua.stubs.LuaStubElementType

/**
 * type for ---@type
 * Created by tangzx on 2017/7/3.
 */
class LuaDocTyType : LuaStubElementType<LuaDocTyStub, LuaDocTypeDef>("DOC_TY"){
    override fun indexStub(stub: LuaDocTyStub, sink: IndexSink) {
    }

    override fun deserialize(inputStream: StubInputStream, stubElement: StubElement<*>?): LuaDocTyStub {
        return LuaDocTyStubImpl(stubElement)
    }

    override fun createPsi(stub: LuaDocTyStub) = LuaDocTypeDefImpl(stub, this)

    override fun serialize(stub: LuaDocTyStub, stubElement: StubOutputStream) {
    }

    override fun createStub(def: LuaDocTypeDef, stubElement: StubElement<*>?): LuaDocTyStub {
        return LuaDocTyStubImpl(stubElement)
    }

    override fun getExternalId() = "lua.doc.type_def"
}