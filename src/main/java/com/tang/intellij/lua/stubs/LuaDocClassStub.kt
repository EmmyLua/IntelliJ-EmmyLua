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
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.comment.psi.impl.LuaDocClassDefImpl
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.psi.aliasName
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.TyClass
import com.tang.intellij.lua.ty.TySerializedClass

/**

 * Created by tangzx on 2016/11/28.
 */
class LuaDocClassType : LuaStubElementType<LuaDocClassStub, LuaDocClassDef>("DOC_CLASS") {

    override fun createPsi(luaDocClassStub: LuaDocClassStub): LuaDocClassDef {
        return LuaDocClassDefImpl(luaDocClassStub, this)
    }

    override fun createStub(luaDocClassDef: LuaDocClassDef, stubElement: StubElement<*>): LuaDocClassStub {
        val superClassNameRef = luaDocClassDef.superClassNameRef
        val superClassName = superClassNameRef?.text
        val aliasName: String? = luaDocClassDef.aliasName

        return LuaDocClassStubImpl(luaDocClassDef.name, aliasName, superClassName, stubElement)
    }

    override fun serialize(luaDocClassStub: LuaDocClassStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(luaDocClassStub.className)
        stubOutputStream.writeName(luaDocClassStub.aliasName)
        stubOutputStream.writeName(luaDocClassStub.superClassName)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaDocClassStub {
        val className = stubInputStream.readName()
        val aliasName = stubInputStream.readName()
        val superClassName = stubInputStream.readName()
        return LuaDocClassStubImpl(StringRef.toString(className)!!,
                StringRef.toString(aliasName),
                StringRef.toString(superClassName),
                stubElement)
    }

    override fun indexStub(luaDocClassStub: LuaDocClassStub, indexSink: IndexSink) {
        val classType = luaDocClassStub.classType
        indexSink.occurrence(StubKeys.CLASS, classType.className)
        indexSink.occurrence(StubKeys.SHORT_NAME, classType.className)

        val superClassName = classType.superClassName
        if (superClassName != null) {
            indexSink.occurrence(StubKeys.SUPER_CLASS, superClassName)
        }
    }
}

interface LuaDocClassStub : StubElement<LuaDocClassDef> {
    val className: String
    val aliasName: String?
    val superClassName: String?
    val classType: TyClass
}

class LuaDocClassStubImpl(override val className: String,
                          override val aliasName: String?,
                          override val superClassName: String?,
                          parent: StubElement<*>)
    : LuaDocStubBase<LuaDocClassDef>(parent, LuaElementType.CLASS_DEF), LuaDocClassStub {

    override val classType: TyClass
        get() {
            val luaType = TySerializedClass(className, className, superClassName)
            luaType.aliasName = aliasName
            return luaType
        }
}