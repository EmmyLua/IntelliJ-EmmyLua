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

package com.tang.intellij.lua.codeInsight.template.macro

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Macro
import com.intellij.codeInsight.template.Result
import com.intellij.util.Processor
import com.tang.intellij.lua.psi.LuaPsiTreeUtilEx

/**
 *
 * Created by TangZX on 2017/4/8.
 */
class SuggestFirstLuaVarNameMacro : Macro() {
    override fun getName(): String {
        return "SuggestFirstLuaVarName"
    }

    override fun getPresentableName(): String {
        return "SuggestFirstLuaVarName()"
    }

    override fun calculateResult(expressions: Array<Expression>, expressionContext: ExpressionContext): Result? {
        return null
    }

    override fun calculateLookupItems(params: Array<out Expression>, context: ExpressionContext): Array<LookupElement>? {
        val list = mutableListOf<LookupElement>()
        LuaPsiTreeUtilEx.walkUpNameDef(context.psiElementAtStartOffset, Processor { nameDef ->
            nameDef.name?.let { list.add(LookupElementBuilder.create(it)) }
            true
        })
        return list.toTypedArray()
    }
}
