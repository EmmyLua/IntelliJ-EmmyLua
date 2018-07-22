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

interface ITySubstitutor {
    fun substitute(function: ITyFunction): ITy
    fun substitute(clazz: ITyClass): ITy
    fun substitute(generic: ITyGeneric): ITy
    fun substitute(ty: ITy): ITy
}

class GenericAnalyzer(arg: ITy, private val par: ITy) : TyVisitor() {
    var cur: ITy = arg

    var map:MutableMap<String, ITy>? = null

    fun analyze(result: MutableMap<String, ITy>) {
        map = result
        warp(cur) { par.accept(this) }
        map = null
    }

    override fun visitClass(clazz: ITyClass) {
        map?.get(clazz.className) ?: return
        map?.merge(clazz.className, cur) { a, b -> a.union(b) }
    }

    override fun visitUnion(u: TyUnion) {
        TyUnion.each(u) { it.accept(this) }
    }

    override fun visitArray(array: ITyArray) {
        TyUnion.each(cur) {
            if (it is ITyArray) {
                warp(it.base) {
                    array.base.accept(this)
                }
            }
        }
    }

    override fun visitFun(f: ITyFunction) {
        TyUnion.each(cur) { it ->
            if (it is ITyFunction) {
                visitSig(it.mainSignature, f.mainSignature)
            }
        }
    }

    override fun visitGeneric(generic: ITyGeneric) {
        TyUnion.each(cur) {
            if (it is ITyGeneric) {
                warp(it.base) {
                    generic.base.accept(this)
                }
                it.params.forEachIndexed { index, iTy ->
                    warp(iTy) {
                        generic.getParamTy(index).accept(this)
                    }
                }
            }
        }
    }

    private fun visitSig(arg: IFunSignature, par: IFunSignature) {
        warp(arg.returnTy) { par.returnTy.accept(this) }
    }

    private fun warp(ty:ITy, action: () -> Unit) {
        if (Ty.isInvalid(ty))
            return
        val arg = cur
        cur = ty
        action()
        cur = arg
    }
}

open class TySubstitutor : ITySubstitutor {
    override fun substitute(ty: ITy) = ty

    override fun substitute(clazz: ITyClass): ITy {
        return clazz
    }

    override fun substitute(generic: ITyGeneric): ITy {
        return generic
    }

    override fun substitute(function: ITyFunction): ITy {
        return TySerializedFunction(function.mainSignature.substitute(this),
                function.signatures.map { it.substitute(this) }.toTypedArray(),
                function.flags)
    }
}