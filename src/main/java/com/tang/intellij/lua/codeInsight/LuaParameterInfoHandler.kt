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
import com.intellij.util.Processor
import com.tang.intellij.lua.psi.LuaArgs
import com.tang.intellij.lua.psi.LuaCallExpr
import com.tang.intellij.lua.psi.LuaListArgs
import com.tang.intellij.lua.psi.LuaTypes
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

data class ParameterInfoType(val sig: IFunSignature, val isColonStyle: Boolean)

/**
 *
 * Created by tangzx on 2016/12/25.
 */
class LuaParameterInfoHandler : ParameterInfoHandler<LuaArgs, ParameterInfoType> {
    override fun couldShowInLookup(): Boolean {
        return false
    }

    override fun getParametersForLookup(lookupElement: LookupElement, parameterInfoContext: ParameterInfoContext): Array<Any>? {
        return emptyArray()
    }

    override fun getParametersForDocumentation(o: ParameterInfoType, parameterInfoContext: ParameterInfoContext): Array<Any>? {
        return emptyArray()
    }

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): LuaArgs? {
        val file = context.file
        val luaArgs = PsiTreeUtil.findElementOfClassAtOffset(file, context.offset, LuaArgs::class.java, false)
        if (luaArgs != null) {
            val callExpr = luaArgs.parent as LuaCallExpr
            val isColonStyle = callExpr.isMethodColonCall
            val type = callExpr.guessParentType(SearchContext(context.project))
            val list = mutableListOf<ParameterInfoType>()
            TyUnion.each(type) { ty ->
                if (ty is ITyFunction) {
                    ty.process(Processor {
                        if ((it.colonCall && !isColonStyle) || it.params.isNotEmpty()) {
                            list.add(ParameterInfoType(it, isColonStyle))
                        }
                        true
                    })
                }
            }
            context.itemsToShow = list.toTypedArray()
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
        if (args is LuaListArgs) {
            val index = ParameterInfoUtils.getCurrentParameterIndex(args.node, context.offset, LuaTypes.COMMA)
            context.setCurrentParameter(index)
        }
    }

    override fun getParameterCloseChars(): String? {
        return ",()"
    }

    override fun tracksParameterIndex(): Boolean {
        return true
    }

    override fun updateUI(o: ParameterInfoType?, context: ParameterInfoUIContext) {
        if (o == null)
            return

        val index = context.currentParameterIndex
        var start = 0
        var end = 0
        val str = buildString {
            o.sig.processArgs(null, o.isColonStyle) { idx, pi ->
                if (idx > 0) append(", ")
                if (idx == index) start = length
                append(pi.name)
                append(":")
                append(pi.ty.displayName)
                if (idx == index) end = length
                true
            }
        }
        if (str.isNotEmpty()) {
            context.setupUIComponentPresentation(
                    str,
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
