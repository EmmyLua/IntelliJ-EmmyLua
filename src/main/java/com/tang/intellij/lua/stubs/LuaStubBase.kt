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

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaPsiElement

abstract class LuaStubBase<T : PsiElement>(parent: StubElement<*>?, type: IStubElementType<*, *>)
    : StubBase<T>(parent, type)

class LuaPlaceholderStub(parent: StubElement<*>?, elementType: IStubElementType<*, *>)
    : LuaStubBase<LuaPsiElement>(parent, elementType) {

    class Type<PsiT : LuaPsiElement>(debugName: String, val ctor: (LuaPlaceholderStub, IStubElementType<*, *>) -> PsiT)
        : IStubElementType<LuaPlaceholderStub, PsiT>(debugName, LuaLanguage.INSTANCE) {
        override fun createStub(psi: PsiT, stubElement: StubElement<*>?): LuaPlaceholderStub {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getExternalId(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun serialize(p0: LuaPlaceholderStub, p1: StubOutputStream) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun deserialize(p0: StubInputStream, p1: StubElement<*>?): LuaPlaceholderStub {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun createPsi(stub: LuaPlaceholderStub): PsiT {
            return ctor(stub, this)
        }

        override fun indexStub(stub: LuaPlaceholderStub, sink: IndexSink) {
        }

    }

}

abstract class LuaDocStubBase<T : PsiElement>(parent: StubElement<*>, type: IStubElementType<StubElement<T>, *>)
    : LuaStubBase<T>(parent, type)