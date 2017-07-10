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

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaNameExpr
import com.tang.intellij.lua.psi.LuaVarList
import com.tang.intellij.lua.psi.impl.LuaNameExprImpl
import com.tang.intellij.lua.stubs.LuaNameStub
import com.tang.intellij.lua.stubs.LuaNameStubImpl
import com.tang.intellij.lua.stubs.index.LuaGlobalVarIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import java.io.IOException

/**
 * global var
 * Created by TangZX on 2017/4/12.
 */
class LuaNameType : IStubElementType<LuaNameStub, LuaNameExpr>("NameExpr", LuaLanguage.INSTANCE) {

    override fun createPsi(luaNameStub: LuaNameStub) = LuaNameExprImpl(luaNameStub, this)

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val psi = node.psi as LuaNameExpr
        return psi.parent is LuaVarList
    }

    override fun createStub(luaNameExpr: LuaNameExpr, stubElement: StubElement<*>): LuaNameStub {
        return LuaNameStubImpl(luaNameExpr, stubElement, this)
    }

    override fun getExternalId() = "lua.name_expr"

    @Throws(IOException::class)
    override fun serialize(luaNameStub: LuaNameStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(luaNameStub.name)
    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaNameStub {
        val nameRef = stubInputStream.readName()
        return LuaNameStubImpl(StringRef.toString(nameRef)!!, stubElement, this)
    }

    override fun indexStub(luaNameStub: LuaNameStub, indexSink: IndexSink) {
        if (luaNameStub.isGlobal) {
            indexSink.occurrence(LuaGlobalVarIndex.KEY, luaNameStub.name)
            indexSink.occurrence(LuaShortNameIndex.KEY, luaNameStub.name)
        }
    }
}
