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
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocFieldDef
import com.tang.intellij.lua.comment.psi.impl.LuaDocFieldDefImpl
import com.tang.intellij.lua.comment.psi.resolveDocTypeSet
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.stubs.LuaDocClassFieldStub
import com.tang.intellij.lua.stubs.LuaDocClassFieldStubImpl
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.LuaShortNameIndex
import com.tang.intellij.lua.ty.Ty
import java.io.IOException

/**

 * Created by tangzx on 2016/12/10.
 */
class LuaDocClassFieldType : IStubElementType<LuaDocClassFieldStub, LuaDocFieldDef>("Class Doc Field", LuaLanguage.INSTANCE) {

    override fun createPsi(luaFieldStub: LuaDocClassFieldStub) = LuaDocFieldDefImpl(luaFieldStub, this)

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val element = node.psi as LuaDocFieldDef
        val comment = LuaCommentUtil.findContainer(element)
        return comment.classDef != null && element.nameIdentifier != null
    }

    override fun createStub(luaDocFieldDef: LuaDocFieldDef, stubElement: StubElement<*>): LuaDocClassFieldStub {
        val comment = LuaCommentUtil.findContainer(luaDocFieldDef)
        val name = luaDocFieldDef.name!!
        val classDef = comment.classDef
        var className: String? = null
        if (classDef != null) {
            className = classDef.name
        }

        val typeSet = resolveDocTypeSet(luaDocFieldDef.typeSet)

        return LuaDocClassFieldStubImpl(stubElement, name, className, typeSet)
    }

    override fun getExternalId() = "lua.class.field"

    @Throws(IOException::class)
    override fun serialize(luaFieldStub: LuaDocClassFieldStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(luaFieldStub.name)
        stubOutputStream.writeName(luaFieldStub.className)
        Ty.serialize(luaFieldStub.type, stubOutputStream)
    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaDocClassFieldStub {
        val name = stubInputStream.readName()
        val className = stubInputStream.readName()
        val typeSet = Ty.deserialize(stubInputStream)
        return LuaDocClassFieldStubImpl(stubElement,
                StringRef.toString(name)!!,
                StringRef.toString(className)!!,
                typeSet)
    }

    override fun indexStub(luaFieldStub: LuaDocClassFieldStub, indexSink: IndexSink) {
        val className = luaFieldStub.className
        className ?: return

        LuaClassMemberIndex.indexStub(indexSink, className, luaFieldStub.name)

        indexSink.occurrence(LuaShortNameIndex.KEY, luaFieldStub.name)
    }
}
