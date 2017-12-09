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
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.psi.LuaNameDef
import com.tang.intellij.lua.psi.LuaParamNameDef
import com.tang.intellij.lua.psi.impl.LuaNameDefImpl
import com.tang.intellij.lua.psi.impl.LuaParamNameDefImpl

class LuaNameDefElementType : LuaStubElementType<LuaNameDefStub, LuaNameDef>("LuaNameDef") {
    override fun indexStub(stub: LuaNameDefStub, sink: IndexSink) {

    }

    override fun createStub(nameDef: LuaNameDef, parentStub: StubElement<*>?): LuaNameDefStub {
        return LuaNameDefStub(nameDef.text, parentStub, LuaElementType.NAME_DEF)
    }

    override fun serialize(stub: LuaNameDefStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
    }

    override fun createPsi(stub: LuaNameDefStub): LuaNameDef {
        return LuaNameDefImpl(stub, LuaElementType.NAME_DEF)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): LuaNameDefStub {
        val name = dataStream.readName()
        return LuaNameDefStub(StringRef.toString(name), parentStub, LuaElementType.NAME_DEF)
    }
}

open class LuaNameDefStub(val name: String, parentStub: StubElement<*>?, type: LuaStubElementType<*, *>)
    : LuaStubBase<LuaNameDef>(parentStub, type)

class ParamNameDefElementType : LuaStubElementType<ParamNameDefStub, LuaParamNameDef>("LuaParamNameDef") {
    override fun indexStub(stub: ParamNameDefStub, sink: IndexSink) {

    }

    override fun createStub(nameDef: LuaParamNameDef, parentStub: StubElement<*>?): ParamNameDefStub {
        return ParamNameDefStub(nameDef.text, parentStub, LuaElementType.PARAM_NAME_DEF)
    }

    override fun serialize(stub: ParamNameDefStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
    }

    override fun createPsi(stub: ParamNameDefStub): LuaParamNameDef {
        return LuaParamNameDefImpl(stub, LuaElementType.PARAM_NAME_DEF)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ParamNameDefStub {
        val name = dataStream.readName()
        return ParamNameDefStub(StringRef.toString(name), parentStub, LuaElementType.PARAM_NAME_DEF)
    }
}

class ParamNameDefStub(name: String, parentStub: StubElement<*>?, type: LuaStubElementType<*, *>)
    : LuaNameDefStub(name, parentStub, type)