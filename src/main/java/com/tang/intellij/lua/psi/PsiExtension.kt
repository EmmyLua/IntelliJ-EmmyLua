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

package com.tang.intellij.lua.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocClassDef
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.getTableTypeName

/**
 * 获取所在的位置
 */
fun LuaLocalDef.getIndexFor(psi: LuaNameDef): Int {
    var idx = 0
    PsiTreeUtil.processElements(this.nameList, {
        if (it is LuaNameDef) {
            if (it == psi)
                return@processElements false
            idx++
        }
        return@processElements true
    })
    return idx
}

fun LuaAssignStat.getIndexFor(psi: LuaExpr): Int {
    var idx = 0
    PsiTreeUtil.processElements(this.varExprList, {
        if (it is LuaExpr) {
            if (it == psi)
                return@processElements false
            idx++
        }
        return@processElements true
    })
    return idx
}

fun LuaAssignStat.getExprAt(index:Int) : LuaExpr? {
    val list = this.varExprList.exprList
    return list[index]
}

fun LuaListArgs.getIndexFor(psi: LuaExpr): Int {
    var idx = 0
    PsiTreeUtil.processElements(this, {
        if (it is LuaExpr) {
            if (it == psi)
                return@processElements false
            idx++
        }
        return@processElements true
    })
    return idx
}

fun LuaExprList.getExprAt(idx: Int): LuaExpr? {
    return exprList[idx]
}

fun LuaParametersOwner.getIndexFor(paramNameDef: LuaParamNameDef): Int {
    val list = this.paramNameDefList
    if (list != null) {
        list.indices
                .filter { list[it].name == paramNameDef.name }
                .forEach { return it }
    }
    return 0
}

fun LuaLocalDef.getExprFor(nameDef: LuaNameDef): LuaExpr? {
    val nameList = this.nameList
    nameList ?: return null
    val exprList = this.exprList
    exprList ?: return null

    var next = nameList.firstChild
    var idx = 0
    var found = false
    while (next != null) {
        if (next is LuaNameDef) {
            if (next == nameDef) {
                found = true
                break
            }
            idx++
        }
        next = next.nextSibling
    }
    if (!found) return null
    return exprList.getExprAt(idx)
}

val LuaParamNameDef.funcBodyOwner: LuaFuncBodyOwner?
    get() = PsiTreeUtil.getParentOfType(this, LuaFuncBodyOwner::class.java)

val LuaParamNameDef.owner: LuaParametersOwner
    get() = PsiTreeUtil.getParentOfType(this, LuaParametersOwner::class.java)!!

enum class LuaLiteralKind {
    String,
    Bool,
    Number,
    Nil,
    Unknown
}

val LuaLiteralExpr.kind: LuaLiteralKind get() = when(node.firstChildNode.elementType) {
    LuaTypes.STRING -> LuaLiteralKind.String
    LuaTypes.TRUE -> LuaLiteralKind.Bool
    LuaTypes.FALSE -> LuaLiteralKind.Bool
    LuaTypes.NIL -> LuaLiteralKind.Nil
    LuaTypes.NUMBER -> LuaLiteralKind.Number
    else -> LuaLiteralKind.Unknown
}

val LuaDocClassDef.aliasName: String? get() {
    val owner = LuaCommentUtil.findOwner(this)
    when (owner) {
        is LuaAssignStat -> {
            val expr = owner.getExprAt(0)
            if (expr != null) return expr.text
        }

        is LuaLocalDef -> {
            val expr = owner.exprList?.getExprAt(0)
            if (expr is LuaTableExpr)
                return getTableTypeName(expr)
        }
    }
    return null
}

val LuaIndexExpr.prefixExpr: LuaExpr get() {
    return firstChild as LuaExpr
}

val LuaTableField.shouldCreateStub: Boolean get() {
    if (id == null && idExpr == null)
        return false
    if (name == null)
        return false

    val tableExpr = PsiTreeUtil.getStubOrPsiParentOfType(this, LuaTableExpr::class.java)
    tableExpr ?: return false
    return tableExpr.shouldCreateStub
}

val LuaTableExpr.shouldCreateStub: Boolean get() {
    val pt = parent
    return when (pt) {
        is LuaTableField -> pt.shouldCreateStub
        is LuaExprList -> {
            val ppt = pt.parent
            when (ppt) {
                is LuaArgs-> false
                else-> true
            }
        }
        else-> true
    }
}

val LuaFuncDef.forwardDeclaration: PsiElement? get() {
    val refName = name
    if (refName != null) {
        return resolveLocal(refName, this, SearchContext(project))
    }
    return null
}

val LuaFuncDef.isGlobal: Boolean get() {
    if (forwardDeclaration != null)
        return false
    if (moduleName != null)
        return false
    return true
}

val LuaCallExpr.argList: List<LuaExpr> get() {
    val args = this.args
    return when (args) {
        is LuaSingleArg -> listOf(args.expr)
        is LuaListArgs -> args.exprList
        else -> emptyList()
    }
}

val LuaBinaryExpr.left: LuaExpr get() {
    return this.firstChild as LuaExpr
}

val LuaBinaryExpr.right: LuaExpr? get() {
    return PsiTreeUtil.nextVisibleLeaf(this.binaryOp) as? LuaExpr?
}