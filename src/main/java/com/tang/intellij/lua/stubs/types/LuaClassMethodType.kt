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
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaClassMethodDefImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.*
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import com.tang.intellij.lua.ty.TyUnion

/**

 * Created by tangzx on 2016/12/4.
 */
class LuaClassMethodType : LuaStubElementType<LuaClassMethodStub, LuaClassMethodDef>("Class Method") {

    override fun createPsi(luaClassMethodStub: LuaClassMethodStub): LuaClassMethodDef {
        return LuaClassMethodDefImpl(luaClassMethodStub, this)
    }

    override fun createStub(methodDef: LuaClassMethodDef, stubElement: StubElement<*>): LuaClassMethodStub {
        val methodName = methodDef.classMethodName
        val id = methodDef.nameIdentifier
        val expr = methodName.expr
        var clazzName = expr.text
        val searchContext = SearchContext(methodDef.project, methodDef.containingFile, true)

        val type = TyUnion.getPerfectClass(expr.guessTypeFromCache(searchContext))
        if (type != null)
            clazzName = type.className

        val isStatic = methodName.dot != null
        val visibility = methodDef.visibility
        val retDocTy = methodDef.comment?.returnDef?.resolveTypeAt(0)
        val params = methodDef.params

        return LuaClassMethodStubImpl(id.text,
                clazzName,
                isStatic,
                visibility,
                retDocTy,
                params,
                stubElement)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        //确定是完整的，并且是 class:method, class.method 形式的， 否则会报错
        val psi = node.psi as LuaClassMethodDef
        return psi.funcBody != null
    }

    override fun serialize(stub: LuaClassMethodStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(stub.className)
        stubOutputStream.writeName(stub.name)

        // is static ?
        stubOutputStream.writeBoolean(stub.isStatic)
        // visibility
        stubOutputStream.writeByte(stub.visibility.ordinal)
        stubOutputStream.writeTyNullable(stub.returnDocTy)
        stubOutputStream.writeParamInfoArray(stub.params)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaClassMethodStub {
        val className = stubInputStream.readName()
        val shortName = stubInputStream.readName()
        val isStatic = stubInputStream.readBoolean()
        val visibility = stubInputStream.readByte()
        val retDocTy = stubInputStream.readTyNullable()
        val params = stubInputStream.readParamInfoArray()
        return LuaClassMethodStubImpl(StringRef.toString(shortName),
                StringRef.toString(className),
                isStatic,
                Visibility.get(visibility.toInt()),
                retDocTy,
                params,
                stubElement)
    }

    override fun indexStub(luaClassMethodStub: LuaClassMethodStub, indexSink: IndexSink) {
        val className = luaClassMethodStub.className
        val shortName = luaClassMethodStub.name

        LuaClassMemberIndex.indexStub(indexSink, className, shortName)
        indexSink.occurrence(LuaShortNameIndex.KEY, shortName)
    }
}
