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
import com.tang.intellij.lua.comment.psi.api.LuaComment
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.getGlobalTypeName
import com.tang.intellij.lua.ty.getTableTypeName

/**
 * 获取所在的位置
 */
fun LuaLocalDef.getIndexFor(psi: LuaNameDef): Int {
    var idx = 0
    val nameList = nameList
    val stub = nameList?.stub
    if (stub != null) {
        idx = stub.childrenStubs.indexOf(psi.stub)
    } else {
        PsiTreeUtil.processElements(nameList, {
            if (it is LuaNameDef) {
                if (it == psi)
                    return@processElements false
                idx++
            }
            return@processElements true
        })
    }
    return idx
}

val LuaNameDef.docTy: ITy? get() {
    val stub = stub
    if (stub != null)
        return stub.docTy

    val localDef = PsiTreeUtil.getParentOfType(this, LuaLocalDef::class.java)
    return localDef?.comment?.docTy
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

val LuaExprList.exprStubList: List<LuaExpr> get() {
    return PsiTreeUtil.getStubChildrenOfTypeAsList(this, LuaExpr::class.java)
}

fun LuaExprList.getExprAt(idx: Int): LuaExpr? {
    return exprStubList[idx]
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
    Unknown;

    companion object {
        fun toEnum(ID: Byte): LuaLiteralKind {
            return LuaLiteralKind.values().find { it.ordinal == ID.toInt() } ?: Unknown
        }
    }
}

val LuaLiteralExpr.kind: LuaLiteralKind get() {
    val stub = this.stub
    if (stub != null)
        return stub.kind

    return when(node.firstChildNode.elementType) {
        LuaTypes.STRING -> LuaLiteralKind.String
        LuaTypes.TRUE -> LuaLiteralKind.Bool
        LuaTypes.FALSE -> LuaLiteralKind.Bool
        LuaTypes.NIL -> LuaLiteralKind.Nil
        LuaTypes.NUMBER -> LuaLiteralKind.Number
        else -> LuaLiteralKind.Unknown
    }
}

val LuaLiteralExpr.stringValue: String get() {
    val content = LuaString.getContent(text)
    return content.value
}

val LuaLiteralExpr.boolValue: Boolean get() = text == "true"

val LuaLiteralExpr.numberValue: Float get() = text.toFloat()

val LuaComment.docTy: ITy? get() {
    return this.typeDef?.type
}

val LuaComment.ty: ITy? get() {
    val cls = classDef?.type
    return cls ?: typeDef?.type
}

val LuaDocClassDef.aliasName: String? get() {
    val owner = LuaCommentUtil.findOwner(this)
    when (owner) {
        is LuaAssignStat -> {
            val expr = owner.getExprAt(0)
            if (expr is LuaNameExpr)
                return getGlobalTypeName(expr)
        }

        is LuaLocalDef -> {
            val expr = owner.exprList?.getExprAt(0)
            if (expr is LuaTableExpr)
                return getTableTypeName(expr)
        }
    }
    return null
}

val LuaIndexExpr.brack: Boolean get() {
    val stub = stub
    return if (stub != null) stub.brack else lbrack != null
}

val LuaIndexExpr.docTy: ITy? get() {
    val stub = stub
    return if (stub != null)
        stub.docTy
    else
        assignStat?.comment?.docTy
}

val LuaIndexExpr.prefixExpr: LuaExpr get() {
    return firstChild as LuaExpr
}

val LuaIndexExpr.assignStat: LuaAssignStat? get() {
    val p1 = parent
    if (p1 is LuaVarList) {
        val p2 = p1.parent
        if (p2 is LuaAssignStat)
            return p2
    }
    return null
}

val LuaNameExpr.assignStat: LuaAssignStat? get() {
    val p1 = parent
    if (p1 is LuaVarList) {
        val p2 = p1.parent
        if (p2 is LuaAssignStat)
            return p2
    }
    return null
}

val LuaNameExpr.docTy: ITy? get() {
    val stub = stub
    if (stub != null)
        return stub.docTy
    return assignStat?.comment?.ty
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

val LuaBinaryExpr.left: LuaExpr get() =
    this.firstChild as LuaExpr

val LuaBinaryExpr.right: LuaExpr? get() =
    PsiTreeUtil.getNextSiblingOfType(binaryOp, LuaExpr::class.java)