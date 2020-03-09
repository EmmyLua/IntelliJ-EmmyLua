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

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.comment.psi.impl.LuaDocTagClassImpl
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.psi.aliasName
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.*

/**

 * Created by tangzx on 2016/11/28.
 */
class LuaDocTagClassType : LuaStubElementType<LuaDocTagClassStub, LuaDocTagClass>("DOC_CLASS") {

    override fun createPsi(luaDocClassStub: LuaDocTagClassStub): LuaDocTagClass {
        return LuaDocTagClassImpl(luaDocClassStub, this)
    }

    override fun createStub(luaDocTagClass: LuaDocTagClass, stubElement: StubElement<*>): LuaDocTagClassStub {
        val params = luaDocTagClass.genericDefList.map { TyParameter(it) }.toTypedArray()
        val superClass = luaDocTagClass.superClassRef?.let { Ty.create(it) }
        val aliasName: String? = luaDocTagClass.aliasName
        return LuaDocTagClassStubImpl(luaDocTagClass.name, params, aliasName, superClass, luaDocTagClass.isDeprecated, luaDocTagClass.isShape, stubElement)
    }

    override fun serialize(luaDocClassStub: LuaDocTagClassStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(luaDocClassStub.className)
        stubOutputStream.writeTyParamsNullable(luaDocClassStub.params)
        stubOutputStream.writeName(luaDocClassStub.aliasName)
        stubOutputStream.writeTyNullable(luaDocClassStub.superClass)
        stubOutputStream.writeBoolean(luaDocClassStub.isDeprecated)
        stubOutputStream.writeBoolean(luaDocClassStub.isShape)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaDocTagClassStub {
        val className = stubInputStream.readName()
        val params = stubInputStream.readTyParamsNullable()
        val aliasName = stubInputStream.readName()
        val superClass = stubInputStream.readTyNullable()
        val isDeprecated = stubInputStream.readBoolean()
        val isShape = stubInputStream.readBoolean()
        return LuaDocTagClassStubImpl(StringRef.toString(className)!!,
                params,
                StringRef.toString(aliasName),
                superClass,
                isDeprecated,
                isShape,
                stubElement)
    }

    override fun indexStub(luaDocClassStub: LuaDocTagClassStub, indexSink: IndexSink) {
        val classType = luaDocClassStub.classType
        indexSink.occurrence(StubKeys.CLASS, classType.className)
        indexSink.occurrence(StubKeys.SHORT_NAME, classType.className)
        val superClass = classType.superClass
        when (superClass) {
            is ITyClass -> indexSink.occurrence(StubKeys.SUPER_CLASS, superClass.className)
            is ITyGeneric -> {
                val base = superClass.base
                if (base is ITyClass) {
                    indexSink.occurrence(StubKeys.SUPER_CLASS, base.className)
                }
            }
        }
    }
}

interface LuaDocTagClassStub : StubElement<LuaDocTagClass> {
    val className: String
    val params: Array<TyParameter>?
    val aliasName: String?
    val superClass: ITy?
    val classType: ITyClass
    val isDeprecated: Boolean
    val isShape: Boolean
}

class LuaDocTagClassStubImpl(override val className: String,
                             override val params: Array<TyParameter>?,
                             override val aliasName: String?,
                             override val superClass: ITy?,
                             override val isDeprecated: Boolean,
                             override val isShape: Boolean,
                             parent: StubElement<*>)
    : LuaDocStubBase<LuaDocTagClass>(parent, LuaElementType.CLASS_DEF), LuaDocTagClassStub {

    override val classType: TyClass
        get() {
            val flags = if (isShape) TyFlags.SHAPE else 0
            val luaType = createSerializedClass(className, params, className, superClass, null, flags)
            luaType.aliasName = aliasName
            return luaType
        }
}
