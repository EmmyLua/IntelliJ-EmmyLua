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
import com.intellij.util.Processor
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.LuaExprPlaceStub
import com.tang.intellij.lua.ty.*

open class LuaCallExprMixin : LuaExprStubMixin<LuaExprPlaceStub> {

    constructor(stub: LuaExprPlaceStub, nodeType: IStubElementType<*, *>)
            : super(stub, nodeType)

    constructor(node: ASTNode) : super(node)

    constructor(stub: LuaExprPlaceStub, nodeType: IElementType, node: ASTNode)
            : super(stub, nodeType, node)

    override fun guessType(context: SearchContext): ITy {
        val luaCallExpr = this as LuaCallExpr
        // xxx()
        val expr = luaCallExpr.expr
        // 从 require 'xxx' 中获取返回类型
        if (expr is LuaNameExpr && expr.name == "require") {
            var filePath: String? = null
            val string = luaCallExpr.firstStringArg
            if (string != null) {
                filePath = string.text
                filePath = filePath!!.substring(1, filePath.length - 1)
            }
            var file: LuaPsiFile? = null
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
                    it.process(Processor { sig ->
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
                ret = bodyOwner.guessReturnType(context)
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