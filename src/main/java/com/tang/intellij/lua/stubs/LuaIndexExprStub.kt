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
import com.intellij.util.BitUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.ITy

/**

 * Created by TangZX on 2017/4/12.
 */
class LuaIndexExprType : LuaStubElementType<LuaIndexExprStub, LuaIndexExpr>("INDEX_EXPR") {

    override fun createPsi(indexStub: LuaIndexExprStub): LuaIndexExpr {
        return LuaIndexExprImpl(indexStub, this)
    }

    override fun createStub(indexExpr: LuaIndexExpr, stubElement: StubElement<*>): LuaIndexExprStub {
        val stat = indexExpr.assignStat
        val docTy = stat?.comment?.docTy
        val classNameSet = mutableSetOf<String>()
        val visibility = indexExpr.visibility

        var flags = BitUtil.set(0, visibility.bitMask, true)
        flags = BitUtil.set(flags, FLAG_DEPRECATED, indexExpr.isDeprecated)
        flags = BitUtil.set(flags, FLAG_BRACK, indexExpr.lbrack != null)
        flags = BitUtil.set(flags, FLAG_ASSIGN, stat != null)

        return LuaIndexExprStubImpl(classNameSet.toTypedArray(),
                indexExpr.name,
                flags,
                docTy,
                indexExpr.worth,
                stubElement,
                this)
    }

    override fun serialize(indexStub: LuaIndexExprStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeNames(indexStub.classNames)
        stubOutputStream.writeName(indexStub.name)
        stubOutputStream.writeInt(indexStub.flags)
        stubOutputStream.writeTyNullable(indexStub.docTy)
        stubOutputStream.writeInt(indexStub.worth)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaIndexExprStub {
        val classNames = stubInputStream.readNames()
        val fieldName = stubInputStream.readName()
        val flags = stubInputStream.readInt()
        val docTy = stubInputStream.readTyNullable()
        val worth = stubInputStream.readInt()
        return LuaIndexExprStubImpl(classNames,
                StringRef.toString(fieldName),
                flags,
                docTy,
                worth,
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

            indexSink.occurrence(StubKeys.SHORT_NAME, fieldName)
        }
    }

    companion object {
        const val FLAG_DEPRECATED = 0x20
        const val FLAG_BRACK = 0x40
        const val FLAG_ASSIGN = 0x80
    }
}

interface LuaIndexExprStub : LuaExprStub<LuaIndexExpr>, LuaClassMemberStub<LuaIndexExpr>, WorthElement {
    val classNames: Array<String>
    val name: String?
    val flags: Int
    val brack: Boolean
    val isAssign: Boolean
}

class LuaIndexExprStubImpl(override val classNames: Array<String>,
                           override val name: String?,
                           override val flags: Int,
                           override val docTy: ITy?,
                           override val worth: Int,
                           stubElement: StubElement<*>,
                           indexType: LuaIndexExprType)
    : LuaStubBase<LuaIndexExpr>(stubElement, indexType), LuaIndexExprStub {
    override val isDeprecated: Boolean
        get() = BitUtil.isSet(flags, LuaIndexExprType.FLAG_DEPRECATED)

    override val visibility: Visibility
        get() = Visibility.getWithMask(flags)

    override val brack: Boolean
        get() = BitUtil.isSet(flags, LuaIndexExprType.FLAG_BRACK)

    override val isAssign: Boolean
        get() = BitUtil.isSet(flags, LuaIndexExprType.FLAG_ASSIGN)
}