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
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.psi.LuaIndexExpr
import com.tang.intellij.lua.psi.LuaVarList
import com.tang.intellij.lua.psi.Visibility
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaIndexExprStub
import com.tang.intellij.lua.stubs.LuaStubElementType
import com.tang.intellij.lua.stubs.impl.LuaIndexExprStubImpl
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaGlobalIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyUnion

/**

 * Created by TangZX on 2017/4/12.
 */
class LuaIndexExprType : LuaStubElementType<LuaIndexExprStub, LuaIndexExpr>("INDEX_EXPR") {

    override fun createPsi(indexStub: LuaIndexExprStub): LuaIndexExpr {
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

    override fun createStub(indexExpr: LuaIndexExpr, stubElement: StubElement<*>): LuaIndexExprStub {
        val context = SearchContext(indexExpr.project, indexExpr.containingFile, true)
        val ty = indexExpr.guessParentType(context)
        val type = TyUnion.getPerfectClass(ty)
        var typeName: String? = null
        if (type != null)
            typeName = type.className
        context.forStore = false
        val valueType = indexExpr.guessValueType(context)
        val visibility = indexExpr.visibility

        return LuaIndexExprStubImpl(typeName,
                indexExpr.name,
                valueType,
                visibility,
                stubElement,
                this)
    }

    override fun serialize(indexStub: LuaIndexExprStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(indexStub.className)
        stubOutputStream.writeName(indexStub.name)
        Ty.serialize(indexStub.valueType, stubOutputStream)
        stubOutputStream.writeByte(indexStub.visibility.ordinal)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaIndexExprStub {
        val typeName = stubInputStream.readName()
        val fieldName = stubInputStream.readName()
        val valueType = Ty.deserialize(stubInputStream)
        val visibility = Visibility.get(stubInputStream.readByte().toInt())
        return LuaIndexExprStubImpl(StringRef.toString(typeName),
                StringRef.toString(fieldName),
                valueType,
                visibility,
                stubElement,
                this)
    }

    override fun indexStub(indexStub: LuaIndexExprStub, indexSink: IndexSink) {
        val fieldName = indexStub.name
        val typeName = indexStub.className
        if (typeName != null && fieldName != null) {
            LuaClassMemberIndex.indexStub(indexSink, typeName, fieldName)

            indexSink.occurrence(LuaShortNameIndex.KEY, fieldName)
            if (typeName == Constants.WORD_G) {
                indexSink.occurrence(LuaGlobalIndex.KEY, fieldName)
            }
        }
    }
}
