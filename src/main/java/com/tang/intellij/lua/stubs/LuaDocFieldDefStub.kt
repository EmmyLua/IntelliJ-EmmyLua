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
import com.intellij.util.BitUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocTagField
import com.tang.intellij.lua.comment.psi.impl.LuaDocTagFieldImpl
import com.tang.intellij.lua.psi.LuaElementType
import com.tang.intellij.lua.psi.Visibility
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

/**

 * Created by tangzx on 2016/12/10.
 */
class LuaDocClassFieldType : LuaStubElementType<LuaDocFieldDefStub, LuaDocTagField>("CLASS_DOC_FIELD") {

    override fun createPsi(stub: LuaDocFieldDefStub) = LuaDocTagFieldImpl(stub, this)

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val element = node.psi as LuaDocTagField
        if (element.nameIdentifier == null)
            return false
        if (element.classNameRef != null)
            return true
        val comment = LuaCommentUtil.findContainer(element)
        return comment.tagClass != null
    }

    override fun createStub(tagField: LuaDocTagField, stubElement: StubElement<*>): LuaDocFieldDefStub {
        val name = tagField.name!!
        var className: String? = null

        val classRef = tagField.classNameRef
        if (classRef != null) {
            className = classRef.id.text
        } else {
            val comment = LuaCommentUtil.findContainer(tagField)
            val classDef = comment.tagClass
            if (classDef != null) {
                className = classDef.name
            }
        }

        var flags = BitUtil.set(0, tagField.visibility.bitMask, true)
        flags = BitUtil.set(flags, FLAG_DEPRECATED, tagField.isDeprecated)

        return LuaDocFieldDefStubImpl(stubElement,
                flags,
                name,
                className,
                tagField.ty?.getType() ?: Ty.UNKNOWN)
    }

    override fun serialize(stub: LuaDocFieldDefStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(stub.name)
        stubOutputStream.writeName(stub.className)
        Ty.serialize(stub.type, stubOutputStream)
        stubOutputStream.writeShort(stub.flags)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaDocFieldDefStub {
        val name = stubInputStream.readName()
        val className = stubInputStream.readName()
        val type = Ty.deserialize(stubInputStream)
        val flags = stubInputStream.readShort()
        return LuaDocFieldDefStubImpl(stubElement,
                flags.toInt(),
                StringRef.toString(name)!!,
                StringRef.toString(className)!!,
                type)
    }

    override fun indexStub(stub: LuaDocFieldDefStub, indexSink: IndexSink) {
        val className = stub.className
        className ?: return

        LuaClassMemberIndex.indexStub(indexSink, className, stub.name)

        indexSink.occurrence(StubKeys.SHORT_NAME, stub.name)
    }

    companion object {
        const val FLAG_DEPRECATED = 0x20
    }
}

interface LuaDocFieldDefStub : LuaClassMemberStub<LuaDocTagField> {
    val name: String

    val type: ITy

    val className: String?

    val flags: Int
}

class LuaDocFieldDefStubImpl(parent: StubElement<*>,
                             override val flags: Int,
                             override val name: String,
                             override val className: String?,
                             override val type: ITy)
    : LuaDocStubBase<LuaDocTagField>(parent, LuaElementType.CLASS_FIELD_DEF), LuaDocFieldDefStub {
    override val docTy = type

    override val isDeprecated: Boolean
        get() = BitUtil.isSet(flags, LuaDocClassFieldType.FLAG_DEPRECATED)

    override val visibility: Visibility
        get() = Visibility.getWithMask(flags)
}