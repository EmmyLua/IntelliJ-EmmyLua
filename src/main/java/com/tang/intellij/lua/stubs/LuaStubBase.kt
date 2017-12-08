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
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.IStubFileElementType
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaPsiElement

abstract class LuaStubBase<T : PsiElement>(parent: StubElement<*>?, type: IStubElementType<*, *>)
    : StubBase<T>(parent, type)

class LuaPlaceholderStub(parent: StubElement<*>?, elementType: IStubElementType<*, *>)
    : LuaStubBase<LuaPsiElement>(parent, elementType) {

    class Type<PsiT : LuaPsiElement>(debugName: String, private val ctor: (LuaPlaceholderStub, IStubElementType<*, *>) -> PsiT)
        : IStubElementType<LuaPlaceholderStub, PsiT>(debugName, LuaLanguage.INSTANCE) {
        override fun createStub(psi: PsiT, parentStub: StubElement<*>?): LuaPlaceholderStub {
            return LuaPlaceholderStub(parentStub, this)
        }

        private fun createStubIfParentIsStub(node: ASTNode): Boolean {
            val parent = node.treeParent
            val parentType = parent.elementType
            return (parentType is IStubElementType<*, *> && parentType.shouldCreateStub(parent)) ||
                    parentType is IStubFileElementType<*>
        }

        override fun shouldCreateStub(node: ASTNode): Boolean {
            return createStubIfParentIsStub(node)
        }

        override fun getExternalId() = "lua.${super.toString()}"

        override fun serialize(stub: LuaPlaceholderStub, dataStream: StubOutputStream) {
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?)
                = LuaPlaceholderStub(parentStub, this)

        override fun createPsi(stub: LuaPlaceholderStub): PsiT {
            return ctor(stub, this)
        }

        override fun indexStub(stub: LuaPlaceholderStub, sink: IndexSink) {
        }
    }
}

abstract class LuaDocStubBase<T : PsiElement>(parent: StubElement<*>, type: IStubElementType<StubElement<T>, *>)
    : LuaStubBase<T>(parent, type)