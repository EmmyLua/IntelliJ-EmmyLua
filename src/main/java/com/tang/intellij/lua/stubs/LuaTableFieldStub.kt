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
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.getTableTypeName
import com.tang.intellij.lua.ty.infer

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
        return if (table != null) getTableTypeName(table) else null
    }

    override fun createStub(field: LuaTableField, parentStub: StubElement<*>): LuaTableFieldStub {
        val className = findTableExprTypeName(field)
        val indexTy = field.indexType?.getType() ?: (field.idExpr as? LuaLiteralExpr)?.infer()
        val flags = BitUtil.set(0, FLAG_DEPRECATED, field.isDeprecated)
        val valueTy = field.comment?.docTy ?: (field.valueExpr as? LuaLiteralExpr)?.infer()

        if (indexTy != null) {
            return LuaTableFieldStubImpl(
                    parentStub,
                    this,
                    className,
                    indexTy,
                    flags,
                    valueTy)
        } else {
            return LuaTableFieldStubImpl(
                    parentStub,
                    this,
                    className,
                    field.fieldName,
                    flags,
                    valueTy)
        }
    }

    override fun serialize(stub: LuaTableFieldStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(stub.className)
        stubOutputStream.writeName(stub.name)
        stubOutputStream.writeTyNullable(stub.indexTy)
        stubOutputStream.writeShort(stub.flags)
        stubOutputStream.writeTyNullable(stub.valueTy)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaTableFieldStub {
        val className = StringRef.toString(stubInputStream.readName())!!
        val name = StringRef.toString(stubInputStream.readName())
        val indexType = stubInputStream.readTyNullable()
        val flags = stubInputStream.readShort().toInt()
        val valueType = stubInputStream.readTyNullable()

        return if (name != null) {
            LuaTableFieldStubImpl(stubElement,
                    this,
                    className,
                    name,
                    flags,
                    valueType)
        } else {
            LuaTableFieldStubImpl(stubElement,
                    this,
                    className,
                    indexType!!,
                    flags,
                    valueType)
        }
    }

    override fun indexStub(fieldStub: LuaTableFieldStub, indexSink: IndexSink) {
        val className = fieldStub.className ?: return
        val fieldName = fieldStub.name

        if (fieldName != null) {
            LuaClassMemberIndex.indexMemberStub(indexSink, className, fieldName)
            indexSink.occurrence(StubKeys.SHORT_NAME, fieldName)
        }

        val indexTy = fieldStub.indexTy ?: return
        LuaClassMemberIndex.indexIndexerStub(indexSink, className, indexTy)
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
    val className: String?

    val name: String?
    val indexTy: ITy?

    val flags: Int

    val valueTy: ITy?

    override val docTy: ITy?
        get() = valueTy
}

class LuaTableFieldStubImpl : LuaStubBase<LuaTableField>, LuaTableFieldStub {
    override val className: String?
    override val name: String?
    override val indexTy: ITy?
    override val flags: Int
    override val valueTy: ITy?

    override val isDeprecated: Boolean
        get() = BitUtil.isSet(flags, LuaDocTagFieldType.FLAG_DEPRECATED)

    override val visibility: Visibility = Visibility.PUBLIC

    constructor(parent: StubElement<*>, elementType: LuaStubElementType<*, *>, className: String?, name: String?, flags: Int, valueTy: ITy?)
            : super(parent, elementType) {
        this.className = className
        this.name = name
        this.indexTy = null
        this.flags = flags
        this.valueTy = valueTy
    }

    constructor(parent: StubElement<*>, elementType: LuaStubElementType<*, *>, className: String?, indexType: ITy, flags: Int, valueTy: ITy?)
            : super(parent, elementType) {
        this.className = className
        this.name = null
        this.indexTy = indexType
        this.flags = flags
        this.valueTy = valueTy
    }
}
