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

import com.intellij.psi.util.PsiTreeUtil


fun LuaAssignStat.getExprAt(index:Int) : LuaExpr? {
    val list = this.varExprList.exprList
    return list[index]
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