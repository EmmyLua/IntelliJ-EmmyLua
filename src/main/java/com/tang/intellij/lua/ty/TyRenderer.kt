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
                        ty.params.forEach { list.add(it.displayName) }

                        val base = ty.base
                        val baseName = if (base is ITyClass) base.className else base.displayName
                        sb.append("${baseName}${renderParamsList(list)}")
                    }
                    is TyParameter -> {

                    }
                    is TySnippet -> sb.append(ty.toString())
                    else -> {
                        error("")
                    }
                }
            }

            override fun visitAlias(alias: ITyAlias) {
                sb.append(alias.name)
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
                val parenthesesRequired = array.base is TyUnion

                if (parenthesesRequired) {
                    sb.append("(")
                }

                array.base.accept(this)

                if (parenthesesRequired) {
                    sb.append(")")
                }

                sb.append("[]")
            }

            override fun visitTuple(multipleResults: TyMultipleResults) {
                val list = multipleResults.list.map { render(it) }
                sb.append(list.joinToString(", "))
                if (multipleResults.variadic) {
                    sb.append("...")
                }
            }
        })
    }

    override fun renderSignature(sb: StringBuilder, signature: IFunSignature) {
        val sig = mutableListOf<String>()
        val params = signature.params
        val varargTy = signature.varargTy

        if (params != null || varargTy != null) {
            params?.forEach {
                sig.add("${it.name}: ${render(it.ty)}")
            }
            varargTy?.let {
                sig.add("...: ${render(it)}")
            }
            sb.append("(${sig.joinToString(", ")})")
        }

        signature.returnTy?.let {
            sb.append(": ")
            render(it, sb)
        }
    }

    open fun renderParamsList(params: Collection<String>?): String {
        return if (params != null && params.isNotEmpty()) "<${params.joinToString(", ")}>" else ""
    }

    open fun renderClass(clazz: ITyClass): String {
        return when {
            clazz is TyDocTable -> {
                val list = mutableListOf<String>()
                clazz.table.tableFieldList.forEach {
                    val key = it.indexType?.getType()?.let { indexTy -> "[${render(indexTy)}]"} ?: it.name
                    it.valueType?.let { ty -> list.add("${key}: ${render(ty.getType())}") }
                }
                "{ ${list.joinToString(", ")} }"
            }
            clazz is TyParameter -> clazz.superClass?.let { "${clazz.varName} : ${it.displayName}" } ?: clazz.varName
            clazz.hasFlag(TyFlags.ANONYMOUS_TABLE) -> renderType(Constants.WORD_TABLE)
            clazz.isAnonymous -> "[local ${clazz.varName}]"
            clazz.isGlobal -> "[global ${clazz.varName}]"
            else -> "${clazz.className}${renderParamsList(clazz.params?.map { it.toString() })}"
        }
    }

    open fun renderType(t: String): String {
        return t
    }

    companion object {
        val SIMPLE: ITyRenderer = TyRenderer()
    }
}
