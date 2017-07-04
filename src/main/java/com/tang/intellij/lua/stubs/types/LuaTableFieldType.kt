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
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.LuaTableField
import com.tang.intellij.lua.psi.impl.LuaTableFieldImpl
import com.tang.intellij.lua.psi.shouldCreateStub
import com.tang.intellij.lua.stubs.LuaTableFieldStub
import com.tang.intellij.lua.stubs.LuaTableFieldStubImpl
import com.tang.intellij.lua.stubs.index.LuaClassFieldIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import java.io.IOException

/**

 * Created by tangzx on 2017/1/14.
 */
class LuaTableFieldType : IStubElementType<LuaTableFieldStub, LuaTableField>("Table Field", LuaLanguage.INSTANCE) {

    override fun createPsi(luaTableFieldStub: LuaTableFieldStub): LuaTableField {
        return LuaTableFieldImpl(luaTableFieldStub, this)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val tableField = node.psi as LuaTableField
        return tableField.shouldCreateStub
    }

    override fun createStub(field: LuaTableField, stubElement: StubElement<*>): LuaTableFieldStub {
        return LuaTableFieldStubImpl(field, stubElement, this)
    }

    override fun getExternalId() = "lua.table_field"

    @Throws(IOException::class)
    override fun serialize(fieldStub: LuaTableFieldStub, stubOutputStream: StubOutputStream) {
        val typeName = fieldStub.typeName
        stubOutputStream.writeName(typeName)
        val fieldName = fieldStub.fieldName
        stubOutputStream.writeName(fieldName)
    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaTableFieldStub {
        val typeName = stubInputStream.readName()
        val fieldName = stubInputStream.readName()
        return LuaTableFieldStubImpl(StringRef.toString(typeName), StringRef.toString(fieldName), stubElement, this)
    }

    override fun indexStub(fieldStub: LuaTableFieldStub, indexSink: IndexSink) {
        val fieldName = fieldStub.fieldName
        val typeName = fieldStub.typeName
        if (fieldName != null && typeName != null) {
            indexSink.occurrence(LuaClassFieldIndex.KEY, typeName)
            indexSink.occurrence(LuaClassFieldIndex.KEY, typeName + "." + fieldName)
            indexSink.occurrence(LuaShortNameIndex.KEY, fieldName)
        }
    }
}
