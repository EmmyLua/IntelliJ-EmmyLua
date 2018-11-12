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
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaClassMethodDefImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.*

/**
 * class method static/instance
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
        val classNameSet = mutableListOf<ITyClass>()

        val searchContext = SearchContext(methodDef.project, methodDef.containingFile, true)
        val ty = expr.guessType(searchContext)
        TyUnion.each(ty) {
            if (it is ITyClass)
                classNameSet.add(it)
        }
        if (classNameSet.isEmpty()) classNameSet.add(createSerializedClass(expr.text))

        var flags = 0

        val isStatic = methodName.dot != null
        val isDeprecated = methodDef.isDeprecated
        flags = BitUtil.set(flags, FLAG_STATIC, isStatic)
        flags = BitUtil.set(flags, FLAG_DEPRECATED, isDeprecated)

        val visibility = methodDef.visibility
        flags = BitUtil.set(flags, visibility.bitMask, true)

        val retDocTy = methodDef.comment?.tagReturn?.type
        val params = methodDef.params
        val overloads = methodDef.overloads
        val tyParams = methodDef.tyParams

        return LuaClassMethodStubImpl(flags,
                id?.text ?: "",
                classNameSet.toTypedArray(),
                retDocTy,
                params,
                tyParams,
                overloads,
                methodDef.varargType,
                stubElement)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        //确定是完整的，并且是 class:method, class.method 形式的， 否则会报错
        val psi = node.psi as LuaClassMethodDef
        return psi.funcBody != null
    }

    private fun StubOutputStream.writeTypes(types: Array<ITyClass>) {
        writeByte(types.size)
        types.forEach { Ty.serialize(it, this) }
    }

    override fun serialize(stub: LuaClassMethodStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeTypes(stub.classes)
        stubOutputStream.writeName(stub.name)
        stubOutputStream.writeShort(stub.flags)
        stubOutputStream.writeTyNullable(stub.returnDocTy)
        stubOutputStream.writeParamInfoArray(stub.params)
        stubOutputStream.writeTyParams(stub.tyParams)
        stubOutputStream.writeTyNullable(stub.varargTy)
        stubOutputStream.writeSignatures(stub.overloads)
    }

    private fun StubInputStream.readTypes(): Array<ITyClass> {
        val size = readByte()
        val list = mutableListOf<ITyClass>()
        for (i in 0 until size) {
            val ty = Ty.deserialize(this) as? ITyClass ?: continue
            list.add(ty)
        }
        return list.toTypedArray()
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaClassMethodStub {
        val classes = stubInputStream.readTypes()
        val shortName = stubInputStream.readName()
        val flags = stubInputStream.readShort()
        val retDocTy = stubInputStream.readTyNullable()
        val params = stubInputStream.readParamInfoArray()
        val tyParams = stubInputStream.readTyParams()
        val varargTy = stubInputStream.readTyNullable()
        val overloads = stubInputStream.readSignatures()

        return LuaClassMethodStubImpl(flags.toInt(),
                StringRef.toString(shortName),
                classes,
                retDocTy,
                params,
                tyParams,
                overloads,
                varargTy,
                stubElement)
    }

    override fun indexStub(luaClassMethodStub: LuaClassMethodStub, indexSink: IndexSink) {
        val classNames = luaClassMethodStub.classes
        val shortName = luaClassMethodStub.name
        classNames.forEach {
            LuaClassMemberIndex.indexStub(indexSink, it.className, shortName)
        }
        indexSink.occurrence(StubKeys.SHORT_NAME, shortName)
    }

    companion object {
        const val FLAG_STATIC = 0x10
        const val FLAG_DEPRECATED = 0x20
    }
}

interface LuaClassMethodStub : LuaFuncBodyOwnerStub<LuaClassMethodDef>, LuaClassMemberStub<LuaClassMethodDef> {

    val classes: Array<ITyClass>

    val name: String

    val isStatic: Boolean

    val flags: Int
}

class LuaClassMethodStubImpl(override val flags: Int,
                             override val name: String,
                             override val classes: Array<ITyClass>,
                             override val returnDocTy: ITy?,
                             override val params: Array<LuaParamInfo>,
                             override val tyParams: Array<TyParameter>,
                             override val overloads: Array<IFunSignature>,
                             override val varargTy: ITy?,
                             parent: StubElement<*>)
    : StubBase<LuaClassMethodDef>(parent, LuaElementType.CLASS_METHOD_DEF), LuaClassMethodStub {
    override val docTy: ITy? = null

    override val isStatic: Boolean
        get() = BitUtil.isSet(flags, LuaClassMethodType.FLAG_STATIC)

    override val isDeprecated: Boolean
        get() = BitUtil.isSet(flags, LuaClassMethodType.FLAG_DEPRECATED)

    override val visibility: Visibility
        get() = Visibility.getWithMask(flags)
}