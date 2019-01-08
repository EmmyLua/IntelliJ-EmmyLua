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
import com.tang.intellij.lua.comment.psi.LuaDocTagType
import com.tang.intellij.lua.comment.psi.impl.LuaDocTagTypeImpl
import com.tang.intellij.lua.psi.LuaElementType

class LuaDocTagTypeType : LuaStubElementType<LuaDocTagTypeStub, LuaDocTagType>("DOC_TY"){
    override fun indexStub(stub: LuaDocTagTypeStub, sink: IndexSink) {
    }

    override fun deserialize(inputStream: StubInputStream, stubElement: StubElement<*>?): LuaDocTagTypeStub {
        return LuaDocTagTypeStubImpl(stubElement)
    }

    override fun createPsi(stub: LuaDocTagTypeStub) = LuaDocTagTypeImpl(stub, this)

    override fun serialize(stub: LuaDocTagTypeStub, stubElement: StubOutputStream) {
    }

    override fun createStub(tagType: LuaDocTagType, stubElement: StubElement<*>?): LuaDocTagTypeStub {
        return LuaDocTagTypeStubImpl(stubElement)
    }
}

interface LuaDocTagTypeStub : StubElement<LuaDocTagType>

class LuaDocTagTypeStubImpl(parent: StubElement<*>?)
    : LuaDocStubBase<LuaDocTagType>(parent, LuaElementType.TYPE_DEF), LuaDocTagTypeStub