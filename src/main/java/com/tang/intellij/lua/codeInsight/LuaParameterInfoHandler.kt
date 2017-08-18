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

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.parameterInfo.*
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.LuaArgs
import com.tang.intellij.lua.psi.LuaCallExpr
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.TyUnion
import java.util.*

/**
 *
 * Created by tangzx on 2016/12/25.
 */
class LuaParameterInfoHandler : ParameterInfoHandler<LuaArgs, LuaFuncBodyOwner> {
    override fun couldShowInLookup(): Boolean {
        return false
    }

    override fun getParametersForLookup(lookupElement: LookupElement, parameterInfoContext: ParameterInfoContext): Array<Any>? {
        return emptyArray()
    }

    override fun getParametersForDocumentation(o: LuaFuncBodyOwner, parameterInfoContext: ParameterInfoContext): Array<Any>? {
        return emptyArray()
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): LuaArgs? {
        val file = context.file
        val luaArgs = PsiTreeUtil.findElementOfClassAtOffset(file, context.offset, LuaArgs::class.java, false)
        if (luaArgs != null) {
            val callExpr = luaArgs.parent as LuaCallExpr
            val bodyOwner = callExpr.resolveFuncBodyOwner(SearchContext(context.project))
            if (bodyOwner != null) {
                val params = bodyOwner.params
                if (params.isEmpty())
                    return null
                context.itemsToShow = arrayOf<Any>(bodyOwner)
            }
        }
        return luaArgs
    }

    override fun showParameterInfo(args: LuaArgs, context: CreateParameterInfoContext) {
        context.showHint(args, args.textRange.startOffset + 1, this)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): LuaArgs? {
        val file = context.file
        return PsiTreeUtil.findElementOfClassAtOffset(file, context.offset, LuaArgs::class.java, false)
    }

    override fun updateParameterInfo(args: LuaArgs, context: UpdateParameterInfoContext) {
        val exprList = args.exprList
        if (exprList != null) {
            val index = ParameterInfoUtils.getCurrentParameterIndex(exprList.node, context.offset, LuaTypes.COMMA)
            context.setCurrentParameter(index)
        }
    }

    override fun getParameterCloseChars(): String? {
        return ",()"
    }

    override fun tracksParameterIndex(): Boolean {
        return true
    }

    override fun updateUI(o: LuaFuncBodyOwner?, context: ParameterInfoUIContext) {
        if (o == null)
            return
        val paramInfos = o.params
        if (paramInfos.isNotEmpty()) {
            val sb = StringBuilder()
            val index = context.currentParameterIndex
            var start = 0
            var end = 0

            for (i in paramInfos.indices) {
                val paramInfo = paramInfos[i]
                if (i > 0)
                    sb.append(", ")
                if (i == index)
                    start = sb.length
                sb.append(paramInfo.name)

                val typeNames = ArrayList<String>()
                TyUnion.each(paramInfo.ty) {
                    typeNames.add(it.createTypeString())
                }
                sb.append(" : ")
                sb.append(typeNames.joinToString("|"))

                if (i == index)
                    end = sb.length
            }

            context.setupUIComponentPresentation(
                    sb.toString(),
                    start,
                    end,
                    false,
                    false,
                    false,
                    context.defaultParameterColor
            )
        }
    }
}
