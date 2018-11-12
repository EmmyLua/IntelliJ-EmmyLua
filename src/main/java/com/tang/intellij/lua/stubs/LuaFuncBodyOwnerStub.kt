/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
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
    val tyParams: Array<TyParameter>
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