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
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyFunction
import com.tang.intellij.lua.ty.TyPsiFunction
import com.tang.intellij.lua.ty.TyUnion

/**
 * 表达式基类
 * Created by TangZX on 2016/12/4.
 */
open class LuaExprMixin internal constructor(node: ASTNode) : LuaPsiElementImpl(node), LuaExpression {

    override fun guessType(context: SearchContext): Ty {
        return RecursionManager.doPreventingRecursion<Ty>(this, true) {
            when {
                this is LuaCallExpr -> guessType(this, context)
                this is LuaParenExpr -> guessType(this, context)
                this is LuaLiteralExpr -> guessType(this)
                this is LuaClosureExpr -> TyPsiFunction(this)
                else -> Ty.UNKNOWN
            }
        }!!
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

    private fun guessType(luaParenExpr: LuaParenExpr, context: SearchContext): Ty {
        val inner = luaParenExpr.expr
        if (inner != null)
            return inner.guessType(context)
        return Ty.UNKNOWN
    }

    private fun guessType(luaCallExpr: LuaCallExpr, context: SearchContext): Ty {
        // xxx()
        val ref = luaCallExpr.expr
        // 从 require 'xxx' 中获取返回类型
        if (ref.textMatches("require")) {
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
        // find in comment
//        val bodyOwner = luaCallExpr.resolveFuncBodyOwner(context)
//        if (bodyOwner != null)
//            return bodyOwner.guessReturnTypeSet(context)

        val ty = ref.guessType(context)
        val tyFunc = TyUnion.find(ty, TyFunction::class.java)
        if (tyFunc != null)
            return tyFunc.returnTy

        return Ty.UNKNOWN
    }
}
