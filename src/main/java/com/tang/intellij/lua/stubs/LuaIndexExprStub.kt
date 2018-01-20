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
import com.tang.intellij.lua.psi.LuaIndexExpr
import com.tang.intellij.lua.psi.Visibility
import com.tang.intellij.lua.psi.assignStat
import com.tang.intellij.lua.psi.docTy
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.ITyClass
import com.tang.intellij.lua.ty.TyUnion

/**

 * Created by TangZX on 2017/4/12.
 */
class LuaIndexExprType : LuaStubElementType<LuaIndexExprStub, LuaIndexExpr>("INDEX_EXPR") {

    override fun createPsi(indexStub: LuaIndexExprStub): LuaIndexExpr {
        return LuaIndexExprImpl(indexStub, this)
    }

    /*override fun shouldCreateStub(node: ASTNode): Boolean {
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
    }*/

    override fun createStub(indexExpr: LuaIndexExpr, stubElement: StubElement<*>): LuaIndexExprStub {
        val stat = indexExpr.assignStat
        val docTy = stat?.comment?.docTy

        val context = SearchContext(indexExpr.project, indexExpr.containingFile, true)
        val ty = indexExpr.guessParentType(context)
        val classNameSet = mutableSetOf<String>()
        TyUnion.each(ty) {
            if (it is ITyClass)
                classNameSet.add(it.className)
        }
        context.forStore = false
        val visibility = indexExpr.visibility

        return LuaIndexExprStubImpl(classNameSet.toTypedArray(),
                indexExpr.name,
                docTy,
                indexExpr.lbrack != null,
                stat != null,
                visibility,
                stubElement,
                this)
    }

    override fun serialize(indexStub: LuaIndexExprStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeNames(indexStub.classNames)
        stubOutputStream.writeName(indexStub.name)
        stubOutputStream.writeTyNullable(indexStub.docTy)
        stubOutputStream.writeBoolean(indexStub.brack)
        stubOutputStream.writeBoolean(indexStub.isAssign)
        stubOutputStream.writeByte(indexStub.visibility.ordinal)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaIndexExprStub {
        val classNames = stubInputStream.readNames()
        val fieldName = stubInputStream.readName()
        val docTy = stubInputStream.readTyNullable()
        val brack = stubInputStream.readBoolean()
        val isAssign = stubInputStream.readBoolean()
        val visibility = Visibility.get(stubInputStream.readByte().toInt())
        return LuaIndexExprStubImpl(classNames,
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
        val classNames = indexStub.classNames
        if (indexStub.isAssign && classNames.isNotEmpty() && fieldName != null) {
            classNames.forEach {
                LuaClassMemberIndex.indexStub(indexSink, it, fieldName)
            }

            indexSink.occurrence(LuaShortNameIndex.KEY, fieldName)
        }
    }
}

interface LuaIndexExprStub : LuaExprStub<LuaIndexExpr>, LuaClassMemberStub<LuaIndexExpr> {
    val classNames: Array<String>
    val name: String?
    val brack: Boolean
    val isAssign: Boolean
}

class LuaIndexExprStubImpl(override val classNames: Array<String>,
                           override val name: String?,
                           override val docTy: ITy?,
                           override val brack: Boolean,
                           override val isAssign: Boolean,
                           override val visibility: Visibility,
                           stubElement: StubElement<*>,
                           indexType: LuaIndexExprType)
    : LuaStubBase<LuaIndexExpr>(stubElement, indexType), LuaIndexExprStub