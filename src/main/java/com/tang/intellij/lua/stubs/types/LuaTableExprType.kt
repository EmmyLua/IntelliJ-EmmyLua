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
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.tang.intellij.lua.psi.LuaTableExpr
import com.tang.intellij.lua.psi.impl.LuaTableExprImpl
import com.tang.intellij.lua.psi.shouldCreateStub
import com.tang.intellij.lua.stubs.LuaStubElementType
import com.tang.intellij.lua.stubs.LuaTableExprStub
import com.tang.intellij.lua.stubs.LuaTableExprStubImpl
import java.io.IOException

/**
 * table
 * Created by tangzx on 2017/1/12.
 */
class LuaTableExprType : LuaStubElementType<LuaTableExprStub, LuaTableExpr>("TABLE") {

    override fun createPsi(luaTableStub: LuaTableExprStub) = LuaTableExprImpl(luaTableStub, this)

    override fun createStub(tableConstructor: LuaTableExpr, stubElement: StubElement<*>): LuaTableExprStub {
        return LuaTableExprStubImpl(stubElement, this)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val tab = node.psi as LuaTableExpr
        return tab.shouldCreateStub
    }

    @Throws(IOException::class)
    override fun serialize(luaTableStub: LuaTableExprStub, stubOutputStream: StubOutputStream) {

    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaTableExprStub {
        return LuaTableExprStubImpl(stubElement, this)
    }

    override fun indexStub(luaTableStub: LuaTableExprStub, indexSink: IndexSink) {}
}
