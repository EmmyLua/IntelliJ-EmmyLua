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

package com.tang.intellij.lua.codeInsight

import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import java.util.*

/**

 * Created by TangZX on 2016/12/14.
 */
class LuaParameterHintsProvider : InlayParameterHintsProvider {

    internal var EXPR_HINT = arrayOf(LuaLiteralExpr::class.java, LuaBinaryExpr::class.java, LuaUnaryExpr::class.java, LuaClosureExpr::class.java)

    override fun getParameterHints(psiElement: PsiElement): List<InlayInfo> {
        val list = ArrayList<InlayInfo>()
        if (psiElement is LuaCallExpr) {
            val callExpr = psiElement
            var parameters: Array<LuaParamInfo>? = null
            val methodDef = callExpr.resolveFuncBodyOwner(SearchContext(psiElement.getProject()))
            methodDef ?: return list


            // 是否是 inst:method() 被用为 inst.method(self) 形式
            var isInstanceMethodUsedAsStaticMethod = false
            var isStaticMethodUsedAsInstanceMethod = false

            parameters = methodDef.params
            if (methodDef is LuaClassMethodDef) {
                isInstanceMethodUsedAsStaticMethod = !methodDef.isStatic && callExpr.isStaticMethodCall
                isStaticMethodUsedAsInstanceMethod = methodDef.isStatic && !callExpr.isStaticMethodCall
            }

            val args = callExpr.args
            val luaExprList = args.exprList
            if (luaExprList != null) {
                val exprList = luaExprList.exprList
                var paramIndex = 0
                val paramCount = parameters.size
                var argIndex = 0

                if (isStaticMethodUsedAsInstanceMethod)
                    paramIndex = 1
                else if (isInstanceMethodUsedAsStaticMethod && exprList.size > 0) {
                    val expr = exprList[argIndex++]
                    list.add(InlayInfo(Constants.WORD_SELF, expr.textOffset))
                }

                while (argIndex < exprList.size && paramIndex < paramCount) {
                    val expr = exprList[argIndex]

                    if (PsiTreeUtil.instanceOf(expr, *EXPR_HINT))
                        list.add(InlayInfo(parameters[paramIndex].name, expr.textOffset))
                    paramIndex++
                    argIndex++
                }
            }
        }

        return list
    }

    override fun getHintInfo(psiElement: PsiElement) = null

    override fun getDefaultBlackList(): Set<String> {
        return HashSet()
    }

    override fun isBlackListSupported() = false

    /*val isDoNotShowIfMethodNameContainsParameterName = Option("java.method.name.contains.parameter.name",
            "Do not show if method name contains parameter name",
            true)

    override fun getSupportedOptions(): List<Option> {
        return listOf(isDoNotShowIfMethodNameContainsParameterName)
    }*/
}
