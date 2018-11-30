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
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.comment.psi.LuaDocTagAlias
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.comment.psi.impl.LuaDocTagAliasImpl
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

class LuaDocTagAliasType : LuaStubElementType<LuaDocTagAliasStub, LuaDocTagAlias>("DOC_TAG_ALIAS") {
    override fun shouldCreateStub(node: ASTNode): Boolean {
        val psi = node.psi as LuaDocTagAlias
        return psi.name != null
    }

    override fun createPsi(stub: LuaDocTagAliasStub): LuaDocTagAlias {
        return  LuaDocTagAliasImpl(stub, this)
    }

    override fun serialize(stub: LuaDocTagAliasStub, stream: StubOutputStream) {
        stream.writeName(stub.name)
        Ty.serialize(stub.type, stream)
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): LuaDocTagAliasStub {
        val name = stream.readName()
        val ty = Ty.deserialize(stream)
        return LuaDocTagAliasStubImpl(StringRef.toString(name), ty, parent)
    }

    override fun createStub(alias: LuaDocTagAlias, parent: StubElement<*>): LuaDocTagAliasStub {
        return LuaDocTagAliasStubImpl(alias.name!!, alias.type, parent)
    }

    override fun indexStub(stub: LuaDocTagAliasStub, sink: IndexSink) {
        sink.occurrence(StubKeys.ALIAS, stub.name)
        sink.occurrence(StubKeys.SHORT_NAME, stub.name)
    }
}

interface LuaDocTagAliasStub : StubElement<LuaDocTagClass> {
    val name: String
    val type: ITy
}

class LuaDocTagAliasStubImpl(
        override val name: String,
        override val type: ITy,
        parent: StubElement<*>
) : LuaDocStubBase<LuaDocTagClass>(parent, LuaElementType.DOC_ALIAS), LuaDocTagAliasStub