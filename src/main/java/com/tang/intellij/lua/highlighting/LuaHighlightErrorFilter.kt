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

package com.tang.intellij.lua.highlighting

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.psi.PsiErrorElement
import com.tang.intellij.lua.comment.psi.LuaDocPsiElement
import com.tang.intellij.lua.project.LuaSettings

/**
 * disable error highlight for doc elements
 * Created by tangzx on 2017/2/19.
 */
class LuaHighlightErrorFilter : HighlightErrorFilter() {
    override fun shouldHighlightErrorElement(psiErrorElement: PsiErrorElement): Boolean {
        val parent = psiErrorElement.parent
        if (parent is LuaDocPsiElement)
            return LuaSettings.instance.isStrictDoc
        return true
    }
}
