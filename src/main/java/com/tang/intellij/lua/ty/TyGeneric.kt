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
import com.tang.intellij.lua.search.SearchContext

abstract class TyGeneric : Ty(TyKind.Generic) {
    override val displayName: String
        get() = "Generic"

    abstract val params: Array<Ty>
    abstract val base: Ty

    fun getParamTy(index: Int): Ty {
        return params.getOrElse(index) { UNKNOWN }
    }
}

class TyDocGeneric(luaDocGenericTy: LuaDocGenericTy, searchContext: SearchContext) : TyGeneric() {

    private fun initBaseTy(luaDocGenericTy: LuaDocGenericTy, searchContext: SearchContext): Ty {
        val firstTyPsi = luaDocGenericTy.firstChild as LuaDocTy
        return firstTyPsi.getType(searchContext)
    }

    private val _baseTy:Ty = initBaseTy(luaDocGenericTy, searchContext)

    private fun initParams(luaDocGenericTy: LuaDocGenericTy, searchContext: SearchContext): Array<Ty> {
        val tyList = luaDocGenericTy.tyList
        val tbl = mutableListOf<Ty>()
        tyList.forEach { tbl.add(it.getType(searchContext)) }
        //第一个是 base
        tbl.removeAt(0)
        return tbl.toTypedArray()
    }

    private val _params: Array<Ty> = initParams(luaDocGenericTy, searchContext)

    override val params: Array<Ty>
        get() = _params
    override val base: Ty
        get() = _baseTy

}

class TySerializedGeneric(override val params: Array<Ty>, override val base: Ty) : TyGeneric()