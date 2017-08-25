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
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.ITyFunction
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyUnion

/**
 * 表达式基类
 * Created by TangZX on 2016/12/4.
 */
open class LuaExprMixin internal constructor(node: ASTNode) : LuaPsiElementImpl(node), LuaExpr {

    override fun guessType(context: SearchContext): ITy {
        val iTy = RecursionManager.doPreventingRecursion<ITy>(this, true) {
            when {
                this is LuaCallExpr -> guessType(this, context)
                this is LuaParenExpr -> guessType(this, context)
                this is LuaLiteralExpr -> guessType(this)
                this is LuaClosureExpr -> asTy(context)
                else -> Ty.UNKNOWN
            }
        }
        return iTy ?: Ty.UNKNOWN
    }

    private fun guessType(literalExpr: LuaLiteralExpr): Ty {
        val child = literalExpr.firstChild
        val type = child.node.elementType
        if (type === LuaTypes.TRUE || type === LuaTypes.FALSE)
            return Ty.BOOLEAN
        if (type === LuaTypes.STRING)
            return Ty.STRING
        if (type === LuaTypes.NUMBER)
            return Ty.NUMBER
        return Ty.UNKNOWN
    }

    private fun guessType(luaParenExpr: LuaParenExpr, context: SearchContext): ITy {
        val inner = luaParenExpr.expr
        if (inner != null)
            return inner.guessType(context)
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
        val ty = expr.guessType(context)
        val tyFunc = TyUnion.find(ty, ITyFunction::class.java)
        if (tyFunc != null)
            ret = tyFunc.returnTy

        //todo TyFunction
        if (Ty.isInvalid(ret)) {
            val bodyOwner = luaCallExpr.resolveFuncBodyOwner(context)
            if (bodyOwner != null)
                ret = bodyOwner.guessReturnTypeSet(context)
        }

        // xxx.new()
        if (expr is LuaIndexExpr) {
            val fnName = expr.name
            if (fnName != null && fnName.equals("new", true)) {
                ret = ret.union(expr.guessPrefixType(context))
            }
        }

        return ret
    }
}
