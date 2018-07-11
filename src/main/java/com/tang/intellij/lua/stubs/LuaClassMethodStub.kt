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
import com.tang.intellij.lua.ty.IFunSignature
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.ITyClass
import com.tang.intellij.lua.ty.TyUnion

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
        val classNameSet = mutableSetOf<String>()

        val searchContext = SearchContext(methodDef.project, methodDef.containingFile, true)
        val ty = expr.guessType(searchContext)
        TyUnion.each(ty) {
            if (it is ITyClass)
                classNameSet.add(it.className)
        }
        if (classNameSet.isEmpty()) classNameSet.add(expr.text)

        var flags = 0

        val isStatic = methodName.dot != null
        val isDeprecated = methodDef.isDeprecated
        flags = BitUtil.set(flags, FLAG_STATIC, isStatic)
        flags = BitUtil.set(flags, FLAG_DEPRECATED, isDeprecated)

        val visibility = methodDef.visibility
        flags = BitUtil.set(flags, visibility.bitMask, true)

        val retDocTy = methodDef.comment?.returnDef?.type
        val params = methodDef.params
        val overloads = methodDef.overloads

        return LuaClassMethodStubImpl(flags,
                id?.text ?: "",
                classNameSet.toTypedArray(),
                retDocTy,
                params,
                overloads,
                stubElement)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        //确定是完整的，并且是 class:method, class.method 形式的， 否则会报错
        val psi = node.psi as LuaClassMethodDef
        return psi.funcBody != null
    }

    override fun serialize(stub: LuaClassMethodStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeNames(stub.classNames)
        stubOutputStream.writeName(stub.name)
        stubOutputStream.writeShort(stub.flags)
        stubOutputStream.writeTyNullable(stub.returnDocTy)
        stubOutputStream.writeParamInfoArray(stub.params)
        stubOutputStream.writeSignatures(stub.overloads)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaClassMethodStub {
        val classNames = stubInputStream.readNames()
        val shortName = stubInputStream.readName()
        val flags = stubInputStream.readShort()
        val retDocTy = stubInputStream.readTyNullable()
        val params = stubInputStream.readParamInfoArray()
        val overloads = stubInputStream.readSignatures()

        return LuaClassMethodStubImpl(flags.toInt(),
                StringRef.toString(shortName),
                classNames,
                retDocTy,
                params,
                overloads,
                stubElement)
    }

    override fun indexStub(luaClassMethodStub: LuaClassMethodStub, indexSink: IndexSink) {
        val classNames = luaClassMethodStub.classNames
        val shortName = luaClassMethodStub.name
        classNames.forEach {
            LuaClassMemberIndex.indexStub(indexSink, it, shortName)
        }
        indexSink.occurrence(StubKeys.SHORT_NAME, shortName)
    }

    companion object {
        const val FLAG_STATIC = 0x10
        const val FLAG_DEPRECATED = 0x20
    }
}

interface LuaClassMethodStub : LuaFuncBodyOwnerStub<LuaClassMethodDef>, LuaClassMemberStub<LuaClassMethodDef> {

    val classNames: Array<String>

    val name: String

    val isStatic: Boolean

    val flags: Int
}

class LuaClassMethodStubImpl(override val flags: Int,
                             override val name: String,
                             override val classNames: Array<String>,
                             override val returnDocTy: ITy?,
                             override val params: Array<LuaParamInfo>,
                             override val overloads: Array<IFunSignature>,
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