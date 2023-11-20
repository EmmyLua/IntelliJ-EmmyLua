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
package com.tang.intellij.lua.codeInsight.postfix.templates

import com.intellij.codeInsight.template.postfix.templates.StringBasedPostfixTemplate
import com.intellij.openapi.util.Conditions
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.codeInsight.postfix.LuaPostfixUtils

/**
 * for k, v in expr do end
 * Created by tangzx on 2017/2/5.
 */
class LuaForIPostfixTemplate : StringBasedPostfixTemplate(
    "for_i",
    "for i, v in ipairs(expr) do end",
    LuaPostfixUtils.selectorTopmost(Conditions.alwaysTrue()),
    null
) {
    override fun getTemplateString(psiElement: PsiElement): String {
        return "for i, v in ipairs(\$expr$) do\n\$END$\nend"
    }

    override fun getElementToRemove(expr: PsiElement): PsiElement {
        return expr
    }
}