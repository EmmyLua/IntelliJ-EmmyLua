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

package com.tang.intellij.lua.ty

import com.tang.intellij.lua.comment.psi.LuaDocGenericTy
import com.tang.intellij.lua.comment.psi.LuaDocTy
import com.tang.intellij.lua.psi.LuaClassMember
import com.tang.intellij.lua.search.SearchContext

class TyParameter(val name:String, base: String? = null) : TySerializedClass(name, name, base) {

    override val kind: TyKind
        get() = TyKind.GenericParam

    override fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean) {
        val superType = getSuperClass(context) as? ITyClass ?: return
        superType.processMembers(context, processor, deep)
    }

    override fun doLazyInit(searchContext: SearchContext) {}
}

interface ITyGeneric : ITy {
    val params: Array<ITy>
    val base: ITy

    fun getParamTy(index: Int): ITy {
        return params.getOrElse(index) { Ty.UNKNOWN }
    }
}

abstract class TyGeneric : Ty(TyKind.Generic), ITyGeneric {

    override fun equals(other: Any?): Boolean {
        return other is ITyGeneric && other.base == base && other.displayName == displayName
    }

    override fun hashCode(): Int {
        return displayName.hashCode()
    }

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        if (super.subTypeOf(other, context, strict)) return true
        if (other !is TyGeneric && base.subTypeOf(other, context, strict)) return true
        return other is TyGeneric
                && base.subTypeOf(other.base, context, strict) // Base should be subtype of other base
                && params.size == other.params.size // Equal amount of params
                && params.indices.all { i -> params[i].subTypeOf(other.params[i], context, strict) } // Params need to be subtypes

    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitGeneric(this)
    }
}

class TyDocGeneric(luaDocGenericTy: LuaDocGenericTy) : TyGeneric() {

    private fun initBaseTy(luaDocGenericTy: LuaDocGenericTy): ITy {
        val firstTyPsi = luaDocGenericTy.firstChild as LuaDocTy
        return firstTyPsi.getType()
    }

    private val _baseTy:ITy = initBaseTy(luaDocGenericTy)

    private fun initParams(luaDocGenericTy: LuaDocGenericTy): Array<ITy> {
        val tyList = luaDocGenericTy.tyList
        val tbl = mutableListOf<ITy>()
        tyList.forEach { tbl.add(it.getType()) }
        //第一个是 base
        tbl.removeAt(0)
        return tbl.toTypedArray()
    }

    private val _params: Array<ITy> = initParams(luaDocGenericTy)

    override val params: Array<ITy>
        get() = _params
    override val base: ITy
        get() = _baseTy

}

class TySerializedGeneric(override val params: Array<ITy>, override val base: ITy) : TyGeneric()