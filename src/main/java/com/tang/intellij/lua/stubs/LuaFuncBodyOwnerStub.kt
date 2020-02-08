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

import com.intellij.openapi.util.Computable
import com.intellij.psi.stubs.StubElement
import com.tang.intellij.lua.ext.recursionGuard
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

/**
 * func body owner stub
 * Created by TangZX on 2017/2/4.
 */
interface LuaFuncBodyOwnerStub<T : LuaFuncBodyOwner> : StubElement<T> {
    val returnDocTy:ITy?
    val params: Array<LuaParamInfo>
    val tyParams: Array<TyParameter>?
    val overloads: Array<IFunSignature>
    val varargTy: ITy?

    private fun walkStub(stub: StubElement<*>, context: SearchContext): ITy? {
        val psi = stub.psi
        return recursionGuard(stub, Computable {
            val ty = when (psi) {
                is LuaReturnStat -> {
                    psi.exprList?.guessTypeAt(context)
                }
                is LuaDoStat,
                is LuaWhileStat,
                is LuaIfStat,
                is LuaForAStat,
                is LuaForBStat,
                is LuaRepeatStat -> {
                    var ret: ITy? = null
                    for (childrenStub in stub.childrenStubs) {
                        ret = walkStub(childrenStub, context)
                        if (ret != null)
                            break
                    }
                    ret
                }
                else -> null
            }
            ty
        })
    }

    fun guessReturnTy(context: SearchContext): ITy {
        val docTy = returnDocTy
        if (docTy != null){
            if (docTy is TyTuple && context.index != -1) {
                return docTy.list.getOrElse(context.index) { Ty.UNKNOWN }
            }
            return docTy
        }
        childrenStubs
                .mapNotNull { walkStub(it, context) }
                .forEach { return it }
        return Ty.VOID
    }
}
