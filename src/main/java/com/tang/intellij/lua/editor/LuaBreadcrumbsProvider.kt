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

package com.tang.intellij.lua.editor

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.*

class LuaBreadcrumbsProvider : BreadcrumbsProvider {
    override fun getLanguages(): Array<Language> {
        return arrayOf(LuaLanguage.INSTANCE)
    }

    override fun getElementInfo(element: PsiElement): String {
        return when (element) {
            is LuaClassMethodDef ->{
                element.classMethodName.text
            }
            is LuaIfStat -> "if"
            is LuaForAStat -> "for"
            is LuaForBStat -> "for"
            is LuaRepeatStat -> "repeat"
            is LuaWhileStat -> "while"
            is LuaClosureExpr -> "function()"
            else -> element.text
        }
    }

    override fun acceptElement(element: PsiElement): Boolean {
        return when (element) {
            is LuaClassMethod -> true
            is LuaIfStat -> true
            is LuaLoop -> true
            is LuaClosureExpr -> true
            else -> false
        }
    }

}