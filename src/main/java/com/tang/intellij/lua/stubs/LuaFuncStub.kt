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
import com.intellij.psi.stubs.*
import com.intellij.util.BitUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaFuncDefImpl
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.IFunSignature
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.TyParameter

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
        val retDocTy = funcDef.comment?.tagReturn?.type
        val params = funcDef.params
        val tyParams = funcDef.tyParams
        val overloads = funcDef.overloads

        var flags = BitUtil.set(0, funcDef.visibility.bitMask, true)
        flags = BitUtil.set(flags, FLAG_DEPRECATED, funcDef.isDeprecated)

        return LuaFuncStubImpl(nameRef.text,
                moduleName,
                flags,
                retDocTy,
                funcDef.varargType,
                params,
                tyParams,
                overloads,
                stubElement)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val element = node.psi as LuaFuncDef
        return element.funcBody != null && element.nameIdentifier != null && element.forwardDeclaration == null
    }

    override fun serialize(stub: LuaFuncStub, stream: StubOutputStream) {
        stream.writeName(stub.name)
        stream.writeName(stub.module)
        stream.writeShort(stub.flags)
        stream.writeTyNullable(stub.returnDocTy)
        stream.writeTyNullable(stub.varargTy)
        stream.writeParamInfoArray(stub.params)
        stream.writeTyParams(stub.tyParams)
        stream.writeSignatures(stub.overloads)
    }

    override fun deserialize(stream: StubInputStream, stubElement: StubElement<*>): LuaFuncStub {
        val name = stream.readName()
        val module = stream.readName()
        val flags = stream.readShort()
        val retDocTy = stream.readTyNullable()
        val varargTy = stream.readTyNullable()
        val params = stream.readParamInfoArray()
        val tyParams = stream.readTyParams()
        val overloads = stream.readSignatures()
        return LuaFuncStubImpl(StringRef.toString(name),
                StringRef.toString(module),
                flags.toInt(),
                retDocTy,
                varargTy,
                params,
                tyParams,
                overloads,
                stubElement)
    }

    override fun indexStub(luaGlobalFuncStub: LuaFuncStub, indexSink: IndexSink) {
        val name = luaGlobalFuncStub.name
        val moduleName = luaGlobalFuncStub.module

        LuaClassMemberIndex.indexStub(indexSink, moduleName, name)

        indexSink.occurrence(StubKeys.SHORT_NAME, name)
    }

    companion object {
        const val FLAG_DEPRECATED = 0x20
    }
}

interface LuaFuncStub : LuaFuncBodyOwnerStub<LuaFuncDef>, LuaClassMemberStub<LuaFuncDef> {
    val name: String
    val module: String
    val flags: Int
}

class LuaFuncStubImpl(override val name: String,
                      override val module: String,
                      override val flags: Int,
                      override val returnDocTy: ITy?,
                      override val varargTy: ITy?,
                      override val params: Array<LuaParamInfo>,
                      override val tyParams: Array<TyParameter>,
                      override val overloads: Array<IFunSignature>,
                      parent: StubElement<*>)
    : StubBase<LuaFuncDef>(parent, LuaTypes.FUNC_DEF as IStubElementType<*, *>), LuaFuncStub {
    override val docTy: ITy?
        get() = null

    override val isDeprecated: Boolean
        get() = BitUtil.isSet(flags, LuaFuncType.FLAG_DEPRECATED)

    override val visibility: Visibility
        get() = Visibility.getWithMask(flags)
}