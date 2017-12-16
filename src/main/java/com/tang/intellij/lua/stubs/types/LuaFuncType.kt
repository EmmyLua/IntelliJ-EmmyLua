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
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.psi.LuaFuncDef
import com.tang.intellij.lua.psi.LuaPsiFile
import com.tang.intellij.lua.psi.forwardDeclaration
import com.tang.intellij.lua.psi.impl.LuaFuncDefImpl
import com.tang.intellij.lua.psi.overloads
import com.tang.intellij.lua.stubs.*
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaGlobalIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex

/**

 * Created by tangzx on 2016/11/26.
 */
class LuaFuncType : LuaStubElementType<LuaFuncStub, LuaFuncDef>("Global Function") {

    override fun createPsi(luaGlobalFuncStub: LuaFuncStub): LuaFuncDef {
        return LuaFuncDefImpl(luaGlobalFuncStub, this)
    }

    override fun createStub(funcDef: LuaFuncDef, stubElement: StubElement<*>): LuaFuncStub {
        val nameRef = funcDef.nameIdentifier!!
        var moduleName = Constants.WORD_G
        val file = funcDef.containingFile
        if (file is LuaPsiFile) moduleName = file.moduleName ?: Constants.WORD_G
        val retDocTy = funcDef.comment?.returnDef?.resolveTypeAt(0)
        val params = funcDef.params
        val overloads = funcDef.overloads

        return LuaFuncStubImpl(nameRef.text,
                moduleName,
                retDocTy,
                params,
                overloads,
                stubElement)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val element = node.psi
        if (element is LuaFuncDef) {
            return element.nameIdentifier != null && element.forwardDeclaration == null
        }
        return false
    }

    override fun serialize(stub: LuaFuncStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(stub.name)
        stubOutputStream.writeName(stub.module)
        stubOutputStream.writeTyNullable(stub.returnDocTy)
        stubOutputStream.writeParamInfoArray(stub.params)
        stubOutputStream.writeSignatures(stub.overloads)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaFuncStub {
        val name = stubInputStream.readName()
        val module = stubInputStream.readName()
        val retDocTy = stubInputStream.readTyNullable()
        val params = stubInputStream.readParamInfoArray()
        val overloads = stubInputStream.readSignatures()
        return LuaFuncStubImpl(StringRef.toString(name),
                StringRef.toString(module),
                retDocTy,
                params,
                overloads,
                stubElement)
    }

    override fun indexStub(luaGlobalFuncStub: LuaFuncStub, indexSink: IndexSink) {
        val name = luaGlobalFuncStub.name
        val moduleName = luaGlobalFuncStub.module

        LuaClassMemberIndex.indexStub(indexSink, moduleName, name)

        indexSink.occurrence(LuaShortNameIndex.KEY, name)
        if (moduleName == Constants.WORD_G)
            indexSink.occurrence(LuaGlobalIndex.KEY, name)
    }
}