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

import com.intellij.codeInsight.daemon.RainbowVisitor
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.LuaDocParamNameRef
import com.tang.intellij.lua.highlighting.LuaHighlightingData
import com.tang.intellij.lua.psi.*

class LuaRainbowVisitor : RainbowVisitor() {
    override fun clone(): HighlightVisitor = LuaRainbowVisitor()

    override fun visit(element: PsiElement) {
        if (element is LuaNameExpr ||
                element is LuaNameDef ||
                element is LuaDocParamNameRef)
        {
            val resolve = when (element) {
                is LuaNameExpr -> resolveLocal(element, null)
                else -> element
            }
            if (resolve is LuaFuncBodyOwner) return
            // exclude 'self'
            if (resolve !is LuaParamNameDef && element.text == Constants.WORD_SELF) return

            val context = PsiTreeUtil.findFirstParent(resolve) { it is LuaFuncBodyOwner }
            if (context != null) {
                val rainbowElement = element

                val name = when (resolve) {
                    is LuaNameDef -> resolve.name
                    else -> rainbowElement.text
                }
                val key = when (resolve) {
                    is LuaParamNameDef -> LuaHighlightingData.PARAMETER
                    else -> null
                }

                val info = getInfo(context, rainbowElement, name, key)
                addInfo(info)
            }
        }
    }

    override fun suitableForFile(p0: PsiFile): Boolean = p0 is LuaPsiFile
}