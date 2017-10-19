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
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Macro
import com.intellij.codeInsight.template.Result
import com.tang.intellij.lua.editor.completion.LuaLookupElement
import java.util.*

/**
 * SuggestTypeMacro
 * Created by TangZX on 2016/12/16.
 */
class SuggestTypeMacro : Macro() {
    override fun getName(): String {
        return "SuggestTypeMacro"
    }

    override fun getPresentableName(): String {
        return "SuggestTypeMacro"
    }

    override fun calculateResult(expressions: Array<Expression>, expressionContext: ExpressionContext): Result? {
        return null
    }

    override fun calculateLookupItems(params: Array<Expression>, context: ExpressionContext): Array<LookupElement>? {
        val list = ArrayList<LookupElement>()
        LuaLookupElement.fillTypes(context.project, list)
        return list.toTypedArray()
    }
}
