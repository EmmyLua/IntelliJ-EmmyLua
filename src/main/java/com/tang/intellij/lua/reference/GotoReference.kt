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

package com.tang.intellij.lua.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import com.tang.intellij.lua.psi.LuaElementFactory
import com.tang.intellij.lua.psi.LuaGotoStat
import com.tang.intellij.lua.psi.LuaLabelStat
import com.tang.intellij.lua.psi.LuaPsiTreeUtil

class GotoReference(val goto: LuaGotoStat)
    : PsiReferenceBase<LuaGotoStat>(goto) {

    val id = goto.id!!

    override fun getVariants(): Array<Any> = arrayOf()

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val newId = LuaElementFactory.createIdentifier(myElement.project, newElementName)
        id.replace(newId)
        return newId
    }

    override fun getRangeInElement(): TextRange {
        val start = id.node.startOffset - myElement.node.startOffset
        return TextRange(start, start + id.textLength)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return myElement.manager.areElementsEquivalent(resolve(), element)
    }

    override fun resolve(): PsiElement? {
        val name = id.text
        var result: PsiElement? = null
        LuaPsiTreeUtil.walkUpLabel(goto) {
            if (it.name == name) {
                result = it
                return@walkUpLabel false
            }
            return@walkUpLabel true
        }
        return result
    }
}