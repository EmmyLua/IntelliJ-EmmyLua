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
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaTableFieldImpl
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.getTableTypeName
import java.util.*

class LuaTableFieldType : LuaStubElementType<LuaTableFieldStub, LuaTableField>("TABLE_FIELD") {

    override fun createPsi(luaTableFieldStub: LuaTableFieldStub): LuaTableField {
        return LuaTableFieldImpl(luaTableFieldStub, this)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val tableField = node.psi as LuaTableField
        return tableField.shouldCreateStub
    }

    private fun findTableExprTypeName(_tableField: LuaTableField): String {
        val table = PsiTreeUtil.getParentOfType(_tableField, LuaTableExpr::class.java)
        val optional = Optional.ofNullable(table)
                .filter { s -> s.parent is LuaExprList }
                .map<PsiElement> { it.parent }
                .filter { s -> s.parent is LuaAssignStat }
                .map<PsiElement> { it.parent }
                .map<String> { s ->
                    val assignStat = s as LuaAssignStat
                    getTypeName(assignStat, 0)
                }
        return optional.orElse(if (table != null) getTableTypeName(table) else null)
    }

    override fun createStub(field: LuaTableField, parentStub: StubElement<*>): LuaTableFieldStub {
        val ty = field.comment?.docTy
        return LuaTableFieldStubImpl(ty,
                field.fieldName,
                findTableExprTypeName(field),
                Visibility.PUBLIC,
                parentStub,
                this)
    }

    override fun serialize(fieldStub: LuaTableFieldStub, stubOutputStream: StubOutputStream) {
        Ty.serializeNullable(fieldStub.docTy, stubOutputStream)
        val fieldName = fieldStub.name
        stubOutputStream.writeName(fieldName)
        stubOutputStream.writeName(fieldStub.typeName)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaTableFieldStub {
        val ty = Ty.deserializeNullable(stubInputStream)
        val fieldName = stubInputStream.readName()
        val typeName = stubInputStream.readName()
        return LuaTableFieldStubImpl(ty,
                StringRef.toString(fieldName),
                StringRef.toString(typeName),
                Visibility.PUBLIC,
                stubElement,
                this)
    }

    override fun indexStub(fieldStub: LuaTableFieldStub, indexSink: IndexSink) {
        val fieldName = fieldStub.name
        val typeName = fieldStub.typeName
        if (fieldName != null && typeName != null) {
            LuaClassMemberIndex.indexStub(indexSink, typeName, fieldName)

            indexSink.occurrence(LuaShortNameIndex.KEY, fieldName)
        }
    }
}

/**
 * table field stub
 * Created by tangzx on 2017/1/14.
 */
interface LuaTableFieldStub : LuaClassMemberStub<LuaTableField> {
    val typeName: String?
    val name: String?
}

class LuaTableFieldStubImpl(
        override val docTy: ITy?,
        override val name: String?,
        override val typeName: String?,
        override val visibility: Visibility,
        parent: StubElement<*>,
        elementType: LuaStubElementType<*, *>
) : LuaStubBase<LuaTableField>(parent, elementType), LuaTableFieldStub