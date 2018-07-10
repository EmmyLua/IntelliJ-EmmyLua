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
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.BitUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaTableFieldImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.TyUnion
import com.tang.intellij.lua.ty.getTableTypeName

class LuaTableFieldType : LuaStubElementType<LuaTableFieldStub, LuaTableField>("TABLE_FIELD") {

    override fun createPsi(luaTableFieldStub: LuaTableFieldStub): LuaTableField {
        return LuaTableFieldImpl(luaTableFieldStub, this)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val tableField = node.psi as LuaTableField
        return tableField.shouldCreateStub
    }

    private fun findTableExprTypeName(field: LuaTableField): String? {
        val table = PsiTreeUtil.getParentOfType(field, LuaTableExpr::class.java)
        val p1 = table?.parent as? LuaExprList
        val p2 = p1?.parent as? LuaAssignStat
        var ty: String? = null
        if (p2 != null) {
            val type = p2.getExprAt(0)?.guessType(SearchContext(p2.project, field.containingFile, true))
            if (type != null) {
                ty = TyUnion.getPerfectClass(type)?.className
            }
        }
        if (ty == null && table != null)
            ty = getTableTypeName(table)
        return ty
    }

    override fun createStub(field: LuaTableField, parentStub: StubElement<*>): LuaTableFieldStub {
        val ty = field.comment?.docTy
        val flags = BitUtil.set(0, FLAG_DEPRECATED, field.isDeprecated)
        return LuaTableFieldStubImpl(ty,
                field.fieldName,
                findTableExprTypeName(field),
                flags,
                parentStub,
                this)
    }

    override fun serialize(fieldStub: LuaTableFieldStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeTyNullable(fieldStub.docTy)
        stubOutputStream.writeName(fieldStub.name)
        stubOutputStream.writeName(fieldStub.typeName)
        stubOutputStream.writeShort(fieldStub.flags)
    }

    override fun deserialize(stream: StubInputStream, stubElement: StubElement<*>): LuaTableFieldStub {
        val ty = stream.readTyNullable()
        val fieldName = stream.readName()
        val typeName = stream.readName()
        val flags = stream.readShort()
        return LuaTableFieldStubImpl(ty,
                StringRef.toString(fieldName),
                StringRef.toString(typeName),
                flags.toInt(),
                stubElement,
                this)
    }

    override fun indexStub(fieldStub: LuaTableFieldStub, indexSink: IndexSink) {
        val fieldName = fieldStub.name
        val typeName = fieldStub.typeName
        if (fieldName != null && typeName != null) {
            LuaClassMemberIndex.indexStub(indexSink, typeName, fieldName)

            indexSink.occurrence(StubKeys.SHORT_NAME, fieldName)
        }
    }

    companion object {
        const val FLAG_DEPRECATED = 0x20
    }
}

/**
 * table field stub
 * Created by tangzx on 2017/1/14.
 */
interface LuaTableFieldStub : LuaClassMemberStub<LuaTableField> {
    val typeName: String?
    val name: String?
    val flags: Int
}

class LuaTableFieldStubImpl(
        override val docTy: ITy?,
        override val name: String?,
        override val typeName: String?,
        override val flags: Int,
        parent: StubElement<*>,
        elementType: LuaStubElementType<*, *>
) : LuaStubBase<LuaTableField>(parent, elementType), LuaTableFieldStub {
    override val isDeprecated: Boolean
        get() = BitUtil.isSet(flags, LuaTableFieldType.FLAG_DEPRECATED)

    override val visibility = Visibility.PUBLIC
}