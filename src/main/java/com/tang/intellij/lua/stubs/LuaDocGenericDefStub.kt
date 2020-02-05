/*
 * Copyright (c) 2020
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
import com.tang.intellij.lua.comment.psi.LuaDocGenericDef
import com.tang.intellij.lua.comment.psi.impl.LuaDocGenericDefImpl
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.ITyClass
import com.tang.intellij.lua.ty.TyClass
import com.tang.intellij.lua.ty.createSerializedClass

class LuaDocGenericDefType : LuaStubElementType<LuaDocGenericDefStub, LuaDocGenericDef>("GENERIC_DEF") {

    override fun createPsi(luaDocGenericDefStub: LuaDocGenericDefStub): LuaDocGenericDef {
        return LuaDocGenericDefImpl(luaDocGenericDefStub, this)
    }

    override fun createStub(luaDocGenericDef: LuaDocGenericDef, stubElement: StubElement<*>): LuaDocGenericDefStub {
        val superClassRef = luaDocGenericDef.classRef
        val superClassName = superClassRef?.classNameRef?.text
        val superClassParams = superClassRef?.tyList?.map { it.text }?.toTypedArray()
        return LuaDocGenericDefStubImpl(luaDocGenericDef.id.text, superClassName, superClassParams, stubElement)
    }

    override fun serialize(luaDocGenericDefStub: LuaDocGenericDefStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(luaDocGenericDefStub.genericName)
        stubOutputStream.writeName(luaDocGenericDefStub.superClassName)
        stubOutputStream.writeParamNames(luaDocGenericDefStub.superClassParams)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaDocGenericDefStub {
        val className = stubInputStream.readName()
        val superClassName = stubInputStream.readName()
        val superClassParams = stubInputStream.readParamNames()
        return LuaDocGenericDefStubImpl(StringRef.toString(className)!!,
                StringRef.toString(superClassName),
                superClassParams,
                stubElement)
    }

    override fun indexStub(luaDocGenericDefStub: LuaDocGenericDefStub, indexSink: IndexSink) {
        val classType = luaDocGenericDefStub.classType
        indexSink.occurrence(StubKeys.GENERIC, classType.className)
        indexSink.occurrence(StubKeys.SHORT_NAME, classType.className)

        val superClassName = classType.superClassName
        if (superClassName != null) {
            indexSink.occurrence(StubKeys.SUPER_CLASS, superClassName)
        }
    }
}

interface LuaDocGenericDefStub : StubElement<LuaDocGenericDef> {
    val genericName: String
    val superClassName: String?
    val superClassParams: Array<String>?
    val classType: ITyClass
}

class LuaDocGenericDefStubImpl(override val genericName: String,
                               override val superClassName: String?,
                               override val superClassParams: Array<String>?,
                               parent: StubElement<*>)
    : LuaDocStubBase<LuaDocGenericDef>(parent, LuaElementType.GENERIC_DEF), LuaDocGenericDefStub {

    override val classType: TyClass
        get() {
            return createSerializedClass(genericName, emptyArray(), genericName, superClassName, superClassParams)
        }
}
