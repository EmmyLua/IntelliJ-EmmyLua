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

package com.tang.intellij.lua.usages

import com.intellij.psi.PsiElement
import com.intellij.usages.UsageTarget
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProviderEx
import com.tang.intellij.lua.psi.LuaCallExpr
import com.tang.intellij.lua.psi.LuaPsiElement

class UsageTypeProvider : UsageTypeProviderEx {
    companion object {
        val FUNCTION_CALL = UsageType("Function call")
    }

    override fun getUsageType(element: PsiElement?, targets: Array<out UsageTarget>): UsageType? {
        if (element is LuaPsiElement) {
            val parent = element.parent
            if (parent is LuaCallExpr)
                return FUNCTION_CALL
        }
        return null
    }

    override fun getUsageType(element: PsiElement?): UsageType? = getUsageType(element, emptyArray())
}