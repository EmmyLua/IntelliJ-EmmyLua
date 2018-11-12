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
import com.tang.intellij.lua.ty.getAnonymousType

class LuaNameDefElementType : LuaStubElementType<LuaNameDefStub, LuaNameDef>("NAME_DEF") {
    override fun indexStub(stub: LuaNameDefStub, sink: IndexSink) {

    }

    override fun createStub(nameDef: LuaNameDef, parentStub: StubElement<*>?): LuaNameDefStub {
        val name = nameDef.name
        val anonymous = getAnonymousType(nameDef)
        val commentOwner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner::class.java)
        val comment = commentOwner?.comment
        val docTy = comment?.tagType?.type ?: comment?.tagClass?.type

        return LuaNameDefStub(name, anonymous, docTy, parentStub, LuaElementType.NAME_DEF)
    }

    override fun serialize(stub: LuaNameDefStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.anonymousType)
        dataStream.writeTyNullable(stub.docTy)
    }

    override fun createPsi(stub: LuaNameDefStub): LuaNameDef {
        return LuaNameDefImpl(stub, LuaElementType.NAME_DEF)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): LuaNameDefStub {
        val name = dataStream.readName()
        val anonymous = dataStream.readName()
        val docTy = dataStream.readTyNullable()
        return LuaNameDefStub(StringRef.toString(name),
                StringRef.toString(anonymous),
                docTy, parentStub, LuaElementType.NAME_DEF)
    }
}

open class LuaNameDefStub(
        val name: String,
        val anonymousType:String,
        override val docTy: ITy?,
        parentStub: StubElement<*>?,
        type: LuaStubElementType<*, *>
) : LuaStubBase<LuaNameDef>(parentStub, type), LuaDocTyStub

class ParamNameDefElementType : LuaStubElementType<ParamNameDefStub, LuaParamNameDef>("PARAM_NAME_DEF") {
    override fun indexStub(stub: ParamNameDefStub, sink: IndexSink) {

    }

    override fun createStub(nameDef: LuaParamNameDef, parentStub: StubElement<*>?): ParamNameDefStub {
        val name = nameDef.name
        val anonymous = getAnonymousType(nameDef)
        val commentOwner = PsiTreeUtil.getParentOfType(nameDef, LuaCommentOwner::class.java)
        val comment = commentOwner?.comment
        val docTy = comment?.getParamDef(name)?.type ?: comment?.tagClass?.type

        return ParamNameDefStub(name, anonymous, docTy, parentStub, LuaElementType.PARAM_NAME_DEF)
    }

    override fun serialize(stub: ParamNameDefStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.anonymousType)
        dataStream.writeTyNullable(stub.docTy)
    }

    override fun createPsi(stub: ParamNameDefStub): LuaParamNameDef {
        return LuaParamNameDefImpl(stub, LuaElementType.PARAM_NAME_DEF)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ParamNameDefStub {
        val name = dataStream.readName()
        val anonymous = dataStream.readName()
        val docTy = dataStream.readTyNullable()
        return ParamNameDefStub(StringRef.toString(name),
                StringRef.toString(anonymous),
                docTy,
                parentStub,
                LuaElementType.PARAM_NAME_DEF)
    }
}

class ParamNameDefStub(
        name: String,
        anonymousType:String,
        docTy: ITy?,
        parentStub: StubElement<*>?,
        type: LuaStubElementType<*, *>
) : LuaNameDefStub(name, anonymousType, docTy, parentStub, type)