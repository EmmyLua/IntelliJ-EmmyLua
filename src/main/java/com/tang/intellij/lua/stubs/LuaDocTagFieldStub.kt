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
class LuaDocTagFieldType : LuaStubElementType<LuaDocTagFieldStub, LuaDocTagField>("CLASS_DOC_FIELD") {

    override fun createPsi(stub: LuaDocTagFieldStub) = LuaDocTagFieldImpl(stub, this)

    override fun shouldCreateStub(node: ASTNode): Boolean {
        val element = node.psi as LuaDocTagField
        if (element.name == null && element.indexType == null)
            return false
        if (element.classNameRef != null)
            return true
        val comment = LuaCommentUtil.findContainer(element)
        return comment.tagClass != null
    }

    override fun createStub(tagField: LuaDocTagField, stubElement: StubElement<*>): LuaDocTagFieldStub {
        val name = tagField.name
        val indexType = tagField.indexType

        val className: String
        val classRef = tagField.classNameRef

        if (classRef != null) {
            className = classRef.id.text
        } else {
            val comment = LuaCommentUtil.findContainer(tagField)
            val classDef = comment.tagClass!!
            className = classDef.name
        }

        val valueTy = tagField.valueType?.getType() ?: Ty.UNKNOWN

        var flags = BitUtil.set(0, tagField.visibility.bitMask, true)
        flags = BitUtil.set(flags, FLAG_DEPRECATED, tagField.isDeprecated)

        return if (name != null) {
            LuaDocFieldDefStubImpl(stubElement,
                    className,
                    name,
                    flags,
                    valueTy)
        } else {
            LuaDocFieldDefStubImpl(stubElement,
                    className,
                    indexType!!.getType(),
                    flags,
                    valueTy)
        }
    }

    override fun serialize(stub: LuaDocTagFieldStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeName(stub.className)
        stubOutputStream.writeName(stub.name)
        stubOutputStream.writeTyNullable(stub.indexTy)
        stubOutputStream.writeShort(stub.flags)
        Ty.serialize(stub.valueTy, stubOutputStream)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaDocTagFieldStub {
        val className = StringRef.toString(stubInputStream.readName())!!
        val name = StringRef.toString(stubInputStream.readName())
        val indexType = stubInputStream.readTyNullable()
        val flags = stubInputStream.readShort().toInt()
        val valueType = Ty.deserialize(stubInputStream)

        return if (name != null) {
            LuaDocFieldDefStubImpl(stubElement,
                    className,
                    name,
                    flags,
                    valueType)
        } else {
            LuaDocFieldDefStubImpl(stubElement,
                    className,
                    indexType!!,
                    flags,
                    valueType)
        }
    }

    override fun indexStub(stub: LuaDocTagFieldStub, indexSink: IndexSink) {
        val className = stub.className ?: return
        val memberName = stub.name

        if (memberName != null) {
            LuaClassMemberIndex.indexMemberStub(indexSink, className, memberName)
            indexSink.occurrence(StubKeys.SHORT_NAME, memberName)
            return
        }

        LuaClassMemberIndex.indexIndexerStub(indexSink, className, stub.indexTy!!)
    }

    companion object {
        const val FLAG_DEPRECATED = 0x20
    }
}

interface LuaDocTagFieldStub : LuaClassMemberStub<LuaDocTagField> {
    val className: String?

    val name: String?
    val indexTy: ITy?

    val flags: Int

    val valueTy: ITy

    override val docTy: ITy
        get() = valueTy
}

class LuaDocFieldDefStubImpl : LuaDocStubBase<LuaDocTagField>, LuaDocTagFieldStub {
    override val className: String
    override val name: String?
    override val indexTy: ITy?
    override val flags: Int
    override val valueTy: ITy

    override val isDeprecated: Boolean
        get() = BitUtil.isSet(flags, LuaDocTagFieldType.FLAG_DEPRECATED)

    override val visibility: Visibility
        get() = Visibility.getWithMask(flags)

    constructor(parent: StubElement<*>, className: String, name: String, flags: Int, valueTy: ITy)
            : super(parent, LuaElementType.CLASS_FIELD_DEF) {
        this.className = className
        this.name = name
        this.indexTy = null
        this.flags = flags
        this.valueTy = valueTy
    }

    constructor(parent: StubElement<*>, className: String, indexType: ITy, flags: Int, valueTy: ITy)
            : super(parent, LuaElementType.CLASS_FIELD_DEF) {
        this.className = className
        this.name = null
        this.indexTy = indexType
        this.flags = flags
        this.valueTy = valueTy
    }
}
