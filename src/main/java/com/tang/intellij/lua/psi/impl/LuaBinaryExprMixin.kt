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

package com.tang.intellij.lua.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.LuaBinaryExpr
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.psi.left
import com.tang.intellij.lua.psi.right
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaBinaryExprStub
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

abstract class LuaBinaryExprMixin : LuaExprStubMixin<LuaBinaryExprStub>, LuaBinaryExpr {
    constructor(stub: LuaBinaryExprStub, nodeType: IStubElementType<*, *>)
            : super(stub, nodeType)

    constructor(node: ASTNode) : super(node)

    constructor(stub: LuaBinaryExprStub, nodeType: IElementType, node: ASTNode)
            : super(stub, nodeType, node)

    override fun guessType(context: SearchContext): ITy {
        val stub = stub
        val operator = if (stub != null) stub.opType else {
            val firstChild = firstChild
            val nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(firstChild)
            nextVisibleLeaf?.node?.elementType
        }
        var ty: ITy = Ty.UNKNOWN
        operator.let {
            ty = when (it) {
            //..
                LuaTypes.CONCAT -> Ty.STRING
            //<=, ==, <, ~=, >=, >
                LuaTypes.LE, LuaTypes.EQ, LuaTypes.LT, LuaTypes.NE, LuaTypes.GE, LuaTypes.GT -> Ty.BOOLEAN
            //and, or
                LuaTypes.AND, LuaTypes.OR -> guessAndOrType(this, operator, context)
            //&, <<, |, >>, ~, ^
                LuaTypes.BIT_AND, LuaTypes.BIT_LTLT, LuaTypes.BIT_OR, LuaTypes.BIT_RTRT, LuaTypes.BIT_TILDE, LuaTypes.EXP,
                    //+, -, *, /, //, %
                LuaTypes.PLUS, LuaTypes.MINUS, LuaTypes.MULT, LuaTypes.DIV, LuaTypes.DOUBLE_DIV, LuaTypes.MOD -> guessBinaryOpType(this, operator, context)
                else -> Ty.UNKNOWN
            }
        }
        return ty
    }

    private fun guessAndOrType(binaryExpr: LuaBinaryExpr, operator: IElementType?, context:SearchContext): ITy {
        val lhs = binaryExpr.left
        val lty = lhs?.guessType(context) ?: Ty.UNKNOWN
        return if (operator == LuaTypes.OR) {
            val rhs = binaryExpr.right
            if (rhs != null) lty.union(rhs.guessType(context)) else lty
        } else {
            lty
        }
    }

    private fun guessBinaryOpType(binaryExpr : LuaBinaryExpr, operator: IElementType?, context:SearchContext): ITy {
        val lhs = binaryExpr.left
        // TODO: Search for operator overrides
        return lhs?.guessType(context) ?: Ty.UNKNOWN
    }
}