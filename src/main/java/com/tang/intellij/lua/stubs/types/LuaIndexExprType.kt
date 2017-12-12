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
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.*
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaGlobalIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
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
        val parent = psi.parent
        if (parent is LuaExprList || parent is LuaCallExpr)
            return super.createStubIfParentIsStub(node)

        if (psi.id != null || psi.idExpr != null) {
            if (parent is LuaVarList) {
                return true
            }
        }
        return false
    }

    override fun createStub(indexExpr: LuaIndexExpr, stubElement: StubElement<*>): LuaIndexExprStub {
        val stat = indexExpr.assignStat
        val docTy = stat?.comment?.docTy

        val context = SearchContext(indexExpr.project, indexExpr.containingFile, true)
        val ty = indexExpr.guessParentType(context)
        val type = TyUnion.getPerfectClass(ty)
        var typeName: String? = null
        if (type != null)
            typeName = type.className
        context.forStore = false
        val visibility = indexExpr.visibility

        return LuaIndexExprStubImpl(typeName,
                indexExpr.name,
                docTy,
                indexExpr.lbrack != null,
                stat != null,
                visibility,
                stubElement,
                this)
    }

    override fun serialize(indexStub: LuaIndexExprStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(indexStub.className)
        stubOutputStream.writeName(indexStub.name)
        stubOutputStream.writeTyNullable(indexStub.docTy)
        stubOutputStream.writeBoolean(indexStub.brack)
        stubOutputStream.writeBoolean(indexStub.isAssign)
        stubOutputStream.writeByte(indexStub.visibility.ordinal)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaIndexExprStub {
        val typeName = stubInputStream.readName()
        val fieldName = stubInputStream.readName()
        val docTy = stubInputStream.readTyNullable()
        val brack = stubInputStream.readBoolean()
        val isAssign = stubInputStream.readBoolean()
        val visibility = Visibility.get(stubInputStream.readByte().toInt())
        return LuaIndexExprStubImpl(StringRef.toString(typeName),
                StringRef.toString(fieldName),
                docTy,
                brack,
                isAssign,
                visibility,
                stubElement,
                this)
    }

    override fun indexStub(indexStub: LuaIndexExprStub, indexSink: IndexSink) {
        val fieldName = indexStub.name
        val typeName = indexStub.className
        if (indexStub.isAssign && typeName != null && fieldName != null) {
            LuaClassMemberIndex.indexStub(indexSink, typeName, fieldName)

            indexSink.occurrence(LuaShortNameIndex.KEY, fieldName)
            if (typeName == Constants.WORD_G) {
                indexSink.occurrence(LuaGlobalIndex.KEY, fieldName)
            }
        }
    }
}
