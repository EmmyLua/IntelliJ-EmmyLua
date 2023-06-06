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

interface ITyRenderer {
    fun render(ty: ITy): String
    fun render(ty: ITy, sb: StringBuilder)
    fun renderSignature(sb: StringBuilder, signature: IFunSignature)
}

open class TyRenderer : TyVisitor(), ITyRenderer {

    override fun render(ty: ITy): String {
        return buildString { render(ty, this) }
    }

    override fun render(ty: ITy, sb: StringBuilder) {
        ty.accept(object : TyVisitor() {
            override fun visitTy(ty: ITy) {
                when (ty) {
                    is ITyPrimitive -> sb.append(renderType(ty.displayName))
                    is TyVoid -> sb.append(renderType(Constants.WORD_VOID))
                    is TyUnknown -> sb.append(renderType(Constants.WORD_ANY))
                    is TyNil -> sb.append(renderType(Constants.WORD_NIL))
                    is ITyGeneric -> {
                        val list = mutableListOf<String>()
                        ty.params.forEach { list.add(renderType(it.displayName)) }
                        sb.append("${renderType(ty.base.displayName)}&lt;${list.joinToString(", ")}&gt;")
                    }
                    is TyParameter -> {

                    }
                    is TyStringLiteral -> sb.append(ty.toString())
                    else -> {
                        error("")
                    }
                }
            }

            override fun visitClass(clazz: ITyClass) {
                sb.append(renderClass(clazz))
            }

            override fun visitUnion(u: TyUnion) {
                val list = mutableSetOf<String>()
                u.acceptChildren(object : TyVisitor() {
                    override fun visitTy(ty: ITy) {
                        val s = render(ty)
                        if (s.isNotEmpty()) list.add(s)
                    }
                })
                sb.append(if (list.isEmpty()) Constants.WORD_ANY else list.joinToString("|"))
            }

            override fun visitFun(f: ITyFunction) {
                sb.append("fun")
                renderSignature(sb, f.mainSignature)
            }

            override fun visitArray(array: ITyArray) {
                array.base.accept(this)
                sb.append("[]")
            }

            override fun visitTuple(tuple: TyTuple) {
                val list = tuple.list.map { render(it) }
                if (list.size <= 1) sb.append(list.joinToString(", "))
                else sb.append("(${list.joinToString(", ")})")
            }
        })
    }

    override fun renderSignature(sb: StringBuilder, signature: IFunSignature) {
        val sig = signature.params.map { "${it.name}: ${render(it.ty)}" }
        sb.append("(${sig.joinToString(", ")}): ")
        render(signature.returnTy, sb)
    }

    open fun renderClass(clazz: ITyClass): String {
        return when {
            clazz is TyDocTable -> {
                val list = mutableListOf<String>()
                clazz.table.tableFieldList.forEach { it.ty?.let { ty-> list.add("${it.name}: ${render(ty.getType())}") } }
                "{ ${list.joinToString(", ")} }"
            }
            clazz.hasFlag(TyFlags.ANONYMOUS_TABLE) -> renderType(Constants.WORD_TABLE)
            clazz.isAnonymous -> "[local ${clazz.varName}]"
            clazz.isGlobal -> "[global ${clazz.varName}]"
            else -> renderType(clazz.className)
        }
    }

    open fun renderType(t: String): String {
        return t
    }

    companion object {
        val SIMPLE: ITyRenderer = TyRenderer()
    }
}