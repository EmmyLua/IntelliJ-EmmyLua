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
import com.tang.intellij.lua.psi.LuaClassMethodDef
import com.tang.intellij.lua.psi.LuaParamInfo
import com.tang.intellij.lua.psi.guessTypeFromCache
import com.tang.intellij.lua.psi.impl.LuaClassMethodDefImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaClassMethodStub
import com.tang.intellij.lua.stubs.LuaClassMethodStubImpl
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyUnion
import java.io.IOException

/**

 * Created by tangzx on 2016/12/4.
 */
class LuaClassMethodType : IStubElementType<LuaClassMethodStub, LuaClassMethodDef>("Class Method", LuaLanguage.INSTANCE) {

    override fun createPsi(luaClassMethodStub: LuaClassMethodStub): LuaClassMethodDef {
        return LuaClassMethodDefImpl(luaClassMethodStub, this)
    }

    override fun createStub(methodDef: LuaClassMethodDef, stubElement: StubElement<*>): LuaClassMethodStub {
        val methodName = methodDef.classMethodName
        val id = methodDef.nameIdentifier
        val expr = methodName.expr
        var clazzName = expr.text
        val searchContext = SearchContext(methodDef.project).setCurrentStubFile(methodDef.containingFile)

        val typeSet = expr.guessTypeFromCache(searchContext)
        val type = TyUnion.getPrefectClass(typeSet)
        if (type != null)
            clazzName = type.className

        val returnTypeSet = methodDef.guessReturnTypeSet(searchContext)
        val params = methodDef.params

        val isStatic = methodName.dot != null

        return LuaClassMethodStubImpl(id.text, clazzName, params, returnTypeSet, isStatic, stubElement)
    }

    override fun getExternalId() = "lua.class_method"

    override fun shouldCreateStub(node: ASTNode): Boolean {
        //确定是完整的，并且是 class:method, class.method 形式的， 否则会报错
        val psi = node.psi as LuaClassMethodDef
        return psi.funcBody != null
    }

    @Throws(IOException::class)
    override fun serialize(luaClassMethodStub: LuaClassMethodStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(luaClassMethodStub.className)
        stubOutputStream.writeName(luaClassMethodStub.name)

        // params
        val params = luaClassMethodStub.params
        stubOutputStream.writeByte(params.size)
        for (param in params) {
            LuaParamInfo.serialize(param, stubOutputStream)
        }

        //return type set
        val returnTypeSet = luaClassMethodStub.returnTypeSet
        Ty.serialize(returnTypeSet, stubOutputStream)

        // is static ?
        stubOutputStream.writeBoolean(luaClassMethodStub.isStatic)
    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaClassMethodStub {
        val className = stubInputStream.readName()
        val shortName = stubInputStream.readName()

        // params
        val len = stubInputStream.readByte().toInt()
        val params = mutableListOf<LuaParamInfo>()
        for (i in 0 until len) {
            params.add(LuaParamInfo.deserialize(stubInputStream))
        }

        val returnTypeSet = Ty.deserialize(stubInputStream)
        val isStatic = stubInputStream.readBoolean()
        return LuaClassMethodStubImpl(StringRef.toString(shortName)!!,
                StringRef.toString(className)!!,
                params.toTypedArray(),
                returnTypeSet,
                isStatic,
                stubElement)
    }

    override fun indexStub(luaClassMethodStub: LuaClassMethodStub, indexSink: IndexSink) {
        val className = luaClassMethodStub.className
        val shortName = luaClassMethodStub.name

        LuaClassMemberIndex.indexStub(indexSink, className, shortName)
        indexSink.occurrence(LuaShortNameIndex.KEY, shortName)
    }
}
