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

import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.psi.LuaCallExpr
import com.tang.intellij.lua.psi.prefixExpr
import com.tang.intellij.lua.search.SearchContext

interface ITySubstitutor {
    fun substitute(alias: ITyAlias): ITy
    fun substitute(function: ITyFunction): ITy
    fun substitute(clazz: ITyClass): ITy
    fun substitute(generic: ITyGeneric): ITy
    fun substitute(ty: ITy): ITy
}

class GenericAnalyzer(params: Array<TyParameter>?, private val searchContext: SearchContext) : TyVisitor() {
    val map: MutableMap<String, ITy> = mutableMapOf()

    private val substitutor = TyParameterSubstitutor(map)
    private val constraints : Map<String, ITy>

    private var cur: ITy

    init {
        val constraints = mutableMapOf<String, ITy>()

        params?.forEach {
            constraints[it.className] = it
        }

        this.constraints = constraints.toMap()
        this.cur = Ty.VOID
    }

    fun analyze(arg: ITy, par: ITy) {
        cur = arg
        warp(cur) { par.accept(this) }
        cur = Ty.VOID
    }

    override fun visitAlias(alias: ITyAlias) {
        alias.ty.accept(this)
    }

    override fun visitClass(clazz: ITyClass) {
        cur.let {
            val clazzParams = clazz.params

            if (clazzParams != null && it is ITyClass) {
                it.params?.asSequence()?.zip(clazzParams.asSequence())?.forEach { (param, clazzParam) ->
                    warp(param) {
                        clazzParam.accept(this)
                    }
                }
            }
        }

        if (clazz is TyParameter) {
            val genericName = clazz.className
            val constraint = constraints.get(genericName)

            if (constraint != null) {
                val mappedType = map.get(genericName)
                val currentType = cur.substitute(substitutor)

                map[genericName] = if (constraint.contravariantOf(currentType, searchContext, TyVarianceFlags.ABSTRACT_PARAMS or TyVarianceFlags.STRICT_UNKNOWN)) {
                    if (mappedType == null) {
                        currentType
                    } else if (mappedType.contravariantOf(currentType, searchContext, TyVarianceFlags.STRICT_UNKNOWN)) {
                        mappedType
                    } else if (currentType.contravariantOf(mappedType, searchContext, TyVarianceFlags.STRICT_UNKNOWN)) {
                        currentType
                    } else {
                        mappedType.union(currentType)
                    }
                } else {
                    constraint
                }
            }
        }
    }

    override fun visitUnion(u: TyUnion) {
        TyUnion.each(u) { it.accept(this) }
    }

    override fun visitArray(array: ITyArray) {
        cur.let {
            if (it is ITyArray) {
                warp(it.base) {
                    array.base.accept(this)
                }
            }
        }
    }

    override fun visitFun(f: ITyFunction) {
        cur.let {
            if (it is ITyFunction) {
                visitSig(it.mainSignature, f.mainSignature)
            }
        }
    }

    override fun visitGeneric(generic: ITyGeneric) {
        cur.let {
            if (it is ITyGeneric) {
                warp(it.base) {
                    generic.base.accept(this)
                }

                it.params.asSequence().zip(generic.params.asSequence()).forEach { (param, genericParam) ->
                    warp(param) {
                        genericParam.accept(this)
                    }
                }
            } else if (generic.base == Ty.TABLE && generic.params.size == 2) {
                if (it == Ty.TABLE) {
                    warp(Ty.UNKNOWN) {
                        generic.params.first().accept(this)
                    }

                    warp(Ty.UNKNOWN) {
                        generic.params.last().accept(this)
                    }
                } else if (it is ITyArray) {
                    warp(Ty.NUMBER) {
                        generic.params.first().accept(this)
                    }

                    warp(it.base) {
                        generic.params.last().accept(this)
                    }
                } else if (it is TyTable) {
                    val generifiedTable = it.toGeneric(searchContext)

                    generifiedTable.params.asSequence().zip(generic.params.asSequence()).forEach { (param, genericParam) ->
                        warp(param) {
                            genericParam.accept(this)
                        }
                    }
                }
            }
        }
    }

    private fun visitSig(arg: IFunSignature, par: IFunSignature) {
        arg.returnTy?.let {
            warp(it) { par.returnTy?.accept(this) }
        }
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

    override fun substitute(alias: ITyAlias): ITy {
        return alias
    }

    override fun substitute(clazz: ITyClass): ITy {
        return clazz
    }

    override fun substitute(generic: ITyGeneric): ITy {
        return TySerializedGeneric(
                generic.params.map { it.substitute(this) }.toTypedArray(),
                generic.base.substitute(this)
        )
    }

    override fun substitute(function: ITyFunction): ITy {
        return TySerializedFunction(function.mainSignature.substitute(this),
                function.signatures.map { it.substitute(this) }.toTypedArray(),
                function.flags)
    }
}

class TyAliasSubstitutor private constructor(val context: SearchContext) : TySubstitutor() {
    val processedNames = mutableSetOf<String>()

    override fun substitute(alias: ITyAlias): ITy {
        if (alias.params?.size ?: 0 == 0) {
            return if (processedNames.add(alias.name)) alias.ty.substitute(this) else Ty.VOID
        }

        return alias
    }

    override fun substitute(generic: ITyGeneric): ITy {
        val base = generic.base.substitute(this)

        if (base is ITyAlias) {
            return if (processedNames.add(base.name)) {
                base.ty.substitute(generic.getMemberSubstitutor(context)).substitute(this)
            } else Ty.VOID
        }

        return super.substitute(generic)
    }

    override fun substitute(clazz: ITyClass): ITy {
        return clazz.recoverAlias(context, this)
    }

    companion object {
        fun substitute(ty: ITy, context: SearchContext): ITy {
            return ty.substitute(TyAliasSubstitutor(context))
        }
    }
}

class TySelfSubstitutor(val context: SearchContext, val call: LuaCallExpr?, val self: ITy? = null) : TySubstitutor() {
    private val selfType: ITy by lazy {
        self ?: (call?.prefixExpr?.guessType(context) ?: Ty.UNKNOWN)
    }

    override fun substitute(clazz: ITyClass): ITy {
        if (clazz.className.endsWith(':' + Constants.WORD_SELF)) {
            return selfType
        }
        return super.substitute(clazz)
    }
}

class TyParameterSubstitutor(val map: Map<String, ITy>) : TySubstitutor() {
    override fun substitute(clazz: ITyClass): ITy {
        return if (clazz is TyParameter) map.get(clazz.className) ?: clazz else clazz
    }
}

class TyChainSubstitutor private constructor(a: ITySubstitutor, b: ITySubstitutor) : ITySubstitutor {
    val substitutors = mutableListOf<ITySubstitutor>()

    init {
        substitutors.add(a)
        substitutors.add(b)
    }

    companion object {
        fun chain(a: ITySubstitutor?, b: ITySubstitutor?): ITySubstitutor? {
            return if (a != null) {
                if (a is TyChainSubstitutor) {
                    b?.let {
                        if (it is TyChainSubstitutor) {
                            a.substitutors.addAll(it.substitutors)
                        } else {
                            a.substitutors.add(it)
                        }
                    }
                    a
                } else {
                    if (b != null) {
                        if (b is TyChainSubstitutor) {
                            b.substitutors.add(0, a)
                            b
                        } else TyChainSubstitutor(a, b)
                    } else a
                }
            } else b
        }
    }

    override fun substitute(alias: ITyAlias): ITy {
        return substitutors.fold(alias as ITy) { ty, subsitutor -> ty.substitute(subsitutor) }
    }

    override fun substitute(function: ITyFunction): ITy {
        return substitutors.fold(function as ITy) { ty, subsitutor -> ty.substitute(subsitutor) }
    }

    override fun substitute(clazz: ITyClass): ITy {
        return substitutors.fold(clazz as ITy) { ty, subsitutor -> ty.substitute(subsitutor) }
    }

    override fun substitute(generic: ITyGeneric): ITy {
        return substitutors.fold(generic as ITy) { ty, subsitutor -> ty.substitute(subsitutor) }
    }

    override fun substitute(ty: ITy): ITy {
        return substitutors.fold(ty) { substitutedTy, subsitutor -> substitutedTy.substitute(subsitutor) }
    }
}
