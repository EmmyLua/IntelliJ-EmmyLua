/*
 * Copyright (c) 2020
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

package com.tang.intellij.lua.ty

import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.readTyParamsNullable
import com.tang.intellij.lua.stubs.writeTyParamsNullable

interface ITyAlias : ITy {
    val name: String
    val params: Array<TyParameter>?
    val ty: ITy
}

class TyAlias(override val name: String,
              override val params: Array<TyParameter>?,
              override val ty: ITy
) : Ty(TyKind.Alias), ITyAlias {

    override fun equals(other: Any?): Boolean {
        return other is ITyAlias && other.name == name && other.flags == flags
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun processMembers(context: SearchContext, processor: (ITy, LuaClassMember) -> Boolean, deep: Boolean): Boolean {
        return ty.processMembers(context, processor, deep)
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return ty.findMember(name, searchContext)
    }

    override fun findIndexer(indexTy: ITy, searchContext: SearchContext): LuaClassMember? {
        return ty.findIndexer(indexTy, searchContext)
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitAlias(this)
    }

    override fun getParams(context: SearchContext): Array<TyParameter>? {
        return params
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        return substitutor.substitute(this)
    }

    override fun contravariantOf(other: ITy, context: SearchContext, flags: Int): Boolean {
        return ty.contravariantOf(other, context, flags) || super.contravariantOf(other, context, flags)
    }
}

object TyAliasSerializer : TySerializer<ITyAlias>() {
    override fun deserializeTy(flags: Int, stream: StubInputStream): ITyAlias {
        val name = stream.readName()
        val params = stream.readTyParamsNullable()
        val ty = Ty.deserialize(stream)
        return TyAlias(StringRef.toString(name), params, ty)
    }

    override fun serializeTy(ty: ITyAlias, stream: StubOutputStream) {
        stream.writeName(ty.name)
        stream.writeTyParamsNullable(ty.params)
        Ty.serialize(ty.ty, stream)
    }
}
