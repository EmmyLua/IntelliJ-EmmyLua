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

package com.tang.intellij.lua.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaIndexExpr
import com.tang.intellij.lua.psi.LuaVarList
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaIndexStub
import com.tang.intellij.lua.stubs.impl.LuaIndexStubImpl
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaGlobalIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyUnion
import java.io.IOException

/**

 * Created by TangZX on 2017/4/12.
 */
class LuaIndexType : IStubElementType<LuaIndexStub, LuaIndexExpr>("LuaIndex", LuaLanguage.INSTANCE) {

    override fun createPsi(indexStub: LuaIndexStub): LuaIndexExpr {
        return LuaIndexExprImpl(indexStub, this)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val psi = node.psi as LuaIndexExpr
        if (psi.id != null || psi.idExpr != null) {
            if (psi.parent is LuaVarList) {
                return true
            }
        }
        return false
    }

    override fun createStub(indexExpr: LuaIndexExpr, stubElement: StubElement<*>): LuaIndexStub {
        val context = SearchContext(indexExpr.project)
        context.setCurrentStubFile(indexExpr.containingFile)
        val ty = indexExpr.guessPrefixType(context)
        val type = TyUnion.getPrefectClass(ty)
        var typeName: String? = null
        if (type != null)
            typeName = type.className
        val valueType = indexExpr.guessValueType(context)

        return LuaIndexStubImpl(typeName, indexExpr.name, valueType, stubElement, this)
    }

    override fun getExternalId() = "lua.index_expr"

    @Throws(IOException::class)
    override fun serialize(indexStub: LuaIndexStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(indexStub.typeName)
        stubOutputStream.writeName(indexStub.name)
        Ty.serialize(indexStub.valueType, stubOutputStream)
    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaIndexStub {
        val typeName = stubInputStream.readName()
        val fieldName = stubInputStream.readName()
        val valueType = Ty.deserialize(stubInputStream)
        return LuaIndexStubImpl(StringRef.toString(typeName), StringRef.toString(fieldName), valueType, stubElement, this)
    }

    override fun indexStub(indexStub: LuaIndexStub, indexSink: IndexSink) {
        val fieldName = indexStub.name
        val typeName = indexStub.typeName
        if (typeName != null && fieldName != null) {
            LuaClassMemberIndex.indexStub(indexSink, typeName, fieldName)

            indexSink.occurrence(LuaShortNameIndex.KEY, fieldName)
            if (typeName == Constants.WORD_G) {
                indexSink.occurrence(LuaGlobalIndex.KEY, fieldName)
            }
        }
    }
}
