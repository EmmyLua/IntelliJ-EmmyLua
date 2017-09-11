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
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaFuncDefImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaFuncStub
import com.tang.intellij.lua.stubs.LuaFuncStubImpl
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex
import com.tang.intellij.lua.stubs.index.LuaGlobalIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import com.tang.intellij.lua.ty.Ty
import java.io.IOException

/**

 * Created by tangzx on 2016/11/26.
 */
class LuaFuncType : IStubElementType<LuaFuncStub, LuaFuncDef>("Global Function", LuaLanguage.INSTANCE) {

    override fun createPsi(luaGlobalFuncStub: LuaFuncStub): LuaFuncDef {
        return LuaFuncDefImpl(luaGlobalFuncStub, this)
    }

    override fun createStub(funcDef: LuaFuncDef, stubElement: StubElement<*>): LuaFuncStub {
        val nameRef = funcDef.nameIdentifier!!
        val searchContext = SearchContext(funcDef.project).setCurrentStubFile(funcDef.containingFile)
        val returnTypeSet = LuaPsiImplUtil.guessReturnTypeSetOriginal(funcDef, searchContext)
        val params = LuaPsiImplUtil.getParamsOriginal(funcDef)
        var moduleName = Constants.WORD_G
        val file = funcDef.containingFile
        if (file is LuaFile) moduleName = file.moduleName ?: Constants.WORD_G

        return LuaFuncStubImpl(nameRef.text, moduleName, params, returnTypeSet, stubElement)
    }

    override fun getExternalId() = "lua.global_func_def"

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val element = node.psi
        if (element is LuaFuncDef) {
            return element.nameIdentifier != null && element.forwardDeclaration == null
        }
        return false
    }

    @Throws(IOException::class)
    override fun serialize(luaGlobalFuncStub: LuaFuncStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(luaGlobalFuncStub.name)
        stubOutputStream.writeName(luaGlobalFuncStub.module)

        // params
        val params = luaGlobalFuncStub.params
        stubOutputStream.writeByte(params.size)
        for (param in params) {
            LuaParamInfo.serialize(param, stubOutputStream)
        }

        Ty.serialize(luaGlobalFuncStub.returnTypeSet, stubOutputStream)
    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaFuncStub {
        val name = stubInputStream.readName()
        val module = stubInputStream.readName()

        // params
        val len = stubInputStream.readByte().toInt()
        val params = mutableListOf<LuaParamInfo>()
        for (i in 0 until len) {
            params.add(LuaParamInfo.deserialize(stubInputStream))
        }

        val returnTypeSet = Ty.deserialize(stubInputStream)
        return LuaFuncStubImpl(StringRef.toString(name)!!,
                StringRef.toString(module)!!,
                params.toTypedArray(),
                returnTypeSet,
                stubElement)
    }

    override fun indexStub(luaGlobalFuncStub: LuaFuncStub, indexSink: IndexSink) {
        val name = luaGlobalFuncStub.name
        val moduleName = luaGlobalFuncStub.module

        indexSink.occurrence(LuaClassMethodIndex.KEY, moduleName)
        indexSink.occurrence(LuaClassMethodIndex.KEY, "$moduleName*$name")

        LuaClassMemberIndex.indexStub(indexSink, moduleName, name)

        indexSink.occurrence(LuaShortNameIndex.KEY, name)
        if (moduleName == Constants.WORD_G)
            indexSink.occurrence(LuaGlobalIndex.KEY, name)
    }
}