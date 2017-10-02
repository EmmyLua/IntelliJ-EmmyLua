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
import com.intellij.openapi.util.RecursionManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

/**
 * 表达式基类
 * Created by TangZX on 2016/12/4.
 */
open class LuaExprMixin internal constructor(node: ASTNode) : LuaPsiElementImpl(node), LuaExpr {

    override fun guessType(context: SearchContext): ITy {
        val iTy = RecursionManager.doPreventingRecursion<ITy>(this, true) {
            when(this) {
                is LuaCallExpr -> guessType(this, context)
                is LuaParenExpr -> guessType(this, context)
                is LuaLiteralExpr -> guessType(this)
                is LuaClosureExpr -> asTy(context)
                is LuaBinaryExpr -> guessType(this)
                else -> Ty.UNKNOWN
            }
        }
        return iTy ?: Ty.UNKNOWN
    }

    private fun guessType(binaryExpr: LuaBinaryExpr): ITy {
        val firstChild = binaryExpr.firstChild
        val nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(firstChild)
        var ty: ITy = Ty.UNKNOWN
        nextVisibleLeaf?.node?.elementType.let {
            ty = when (it) {
                //..
                LuaTypes.CONCAT -> Ty.STRING
                //and, or, <=, ==, <, ~=, >=, >
                LuaTypes.AND, LuaTypes.OR, LuaTypes.LE, LuaTypes.EQ, LuaTypes.LT, LuaTypes.NE, LuaTypes.GE, LuaTypes.GT -> Ty.BOOLEAN
                //&, <<, |, >>, ~, ^
                LuaTypes.BIT_AND, LuaTypes.BIT_LTLT, LuaTypes.BIT_OR, LuaTypes.BIT_RTRT, LuaTypes.BIT_TILDE, LuaTypes.EXP,
                //+, -, *, /, //, %
                LuaTypes.PLUS, LuaTypes.MINUS, LuaTypes.MULT, LuaTypes.DIV, LuaTypes.DOUBLE_DIV, LuaTypes.MOD -> Ty.NUMBER
                else -> Ty.UNKNOWN
            }
        }
        return ty
    }

    private fun guessType(literalExpr: LuaLiteralExpr): ITy {
        val child = literalExpr.firstChild
        return when (child.node.elementType) {
            LuaTypes.TRUE ,LuaTypes.FALSE -> Ty.BOOLEAN
            LuaTypes.STRING -> Ty.STRING
            LuaTypes.NUMBER -> Ty.NUMBER
            LuaTypes.NIL -> Ty.NIL
            else -> Ty.UNKNOWN
        }
    }

    private fun guessType(luaParenExpr: LuaParenExpr, context: SearchContext): ITy {
        val inner = luaParenExpr.expr
        if (inner != null)
            return inner.guessTypeFromCache(context)
        return Ty.UNKNOWN
    }

    private fun guessType(luaCallExpr: LuaCallExpr, context: SearchContext): ITy {
        // xxx()
        val expr = luaCallExpr.expr
        // 从 require 'xxx' 中获取返回类型
        if (expr.textMatches("require")) {
            var filePath: String? = null
            val string = luaCallExpr.firstStringArg
            if (string != null) {
                filePath = string.text
                filePath = filePath!!.substring(1, filePath.length - 1)
            }
            var file: LuaFile? = null
            if (filePath != null)
                file = resolveRequireFile(filePath, luaCallExpr.project)
            if (file != null)
                return file.getReturnedType(context)

            return Ty.UNKNOWN
        }

        var ret: ITy = Ty.UNKNOWN
        val ty = expr.guessTypeFromCache(context)
        TyUnion.each(ty) {
            when(it) {
                is ITyFunction -> {
                    it.process(Processor {sig ->
                        ret = ret.union(sig.returnTy)
                        true
                    })
                }
                //constructor : Class table __call
                is ITyClass -> ret = ret.union(it)
            }
        }

        //todo TyFunction
        if (Ty.isInvalid(ret)) {
            val bodyOwner = luaCallExpr.resolveFuncBodyOwner(context)
            if (bodyOwner != null)
                ret = bodyOwner.guessReturnTypeSet(context)
        }

        // xxx.new()
        if (expr is LuaIndexExpr) {
            val fnName = expr.name
            if (fnName != null && LuaSettings.isConstructorName(fnName)) {
                ret = ret.union(expr.guessParentType(context))
            }
        }

        return ret
    }
}
