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
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.psi.LuaCommentOwner
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.psi.LuaNameDef
import com.tang.intellij.lua.psi.LuaParamNameDef
import com.tang.intellij.lua.psi.impl.LuaNameDefImpl
import com.tang.intellij.lua.psi.impl.LuaParamNameDefImpl
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

class LuaNameDefElementType : LuaStubElementType<LuaNameDefStub, LuaNameDef>("NAME_DEF") {
    override fun indexStub(stub: LuaNameDefStub, sink: IndexSink) {

    }

    override fun createStub(nameDef: LuaNameDef, parentStub: StubElement<*>?): LuaNameDefStub {
        val name = nameDef.name

        val commentOwner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner::class.java)
        val comment = commentOwner?.comment
        val docTy = comment?.typeDef?.type ?: comment?.classDef?.type

        return LuaNameDefStub(name, docTy, parentStub, LuaElementType.NAME_DEF)
    }

    override fun serialize(stub: LuaNameDefStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        Ty.serializeNullable(stub.docTy, dataStream)
    }

    override fun createPsi(stub: LuaNameDefStub): LuaNameDef {
        return LuaNameDefImpl(stub, LuaElementType.NAME_DEF)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): LuaNameDefStub {
        val name = dataStream.readName()
        val docTy = Ty.deserializeNullable(dataStream)
        return LuaNameDefStub(StringRef.toString(name), docTy, parentStub, LuaElementType.NAME_DEF)
    }
}

open class LuaNameDefStub(
        val name: String,
        override val docTy: ITy?,
        parentStub: StubElement<*>?,
        type: LuaStubElementType<*, *>
) : LuaStubBase<LuaNameDef>(parentStub, type), LuaDocTyStub

class ParamNameDefElementType : LuaStubElementType<ParamNameDefStub, LuaParamNameDef>("PARAM_NAME_DEF") {
    override fun indexStub(stub: ParamNameDefStub, sink: IndexSink) {

    }

    override fun createStub(nameDef: LuaParamNameDef, parentStub: StubElement<*>?): ParamNameDefStub {
        val name = nameDef.name

        val commentOwner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner::class.java)
        val comment = commentOwner?.comment
        val docTy = comment?.getParamDef(name)?.type ?: comment?.classDef?.type

        return ParamNameDefStub(name, docTy, parentStub, LuaElementType.PARAM_NAME_DEF)
    }

    override fun serialize(stub: ParamNameDefStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
    }

    override fun createPsi(stub: ParamNameDefStub): LuaParamNameDef {
        return LuaParamNameDefImpl(stub, LuaElementType.PARAM_NAME_DEF)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ParamNameDefStub {
        val name = dataStream.readName()
        val docTy = Ty.deserializeNullable(dataStream)
        return ParamNameDefStub(StringRef.toString(name), docTy, parentStub, LuaElementType.PARAM_NAME_DEF)
    }
}

class ParamNameDefStub(
        name: String,
        docTy: ITy?,
        parentStub: StubElement<*>?,
        type: LuaStubElementType<*, *>
) : LuaNameDefStub(name, docTy, parentStub, type)