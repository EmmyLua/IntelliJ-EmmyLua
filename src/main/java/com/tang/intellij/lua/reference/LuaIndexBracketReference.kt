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
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import com.tang.intellij.lua.lang.type.LuaString
import com.tang.intellij.lua.psi.LuaElementFactory
import com.tang.intellij.lua.psi.LuaIndexExpr
import com.tang.intellij.lua.psi.LuaLiteralExpr
import com.tang.intellij.lua.psi.resolve
import com.tang.intellij.lua.search.SearchContext

class LuaIndexBracketReference internal constructor(element: LuaIndexExpr, private val id: LuaLiteralExpr)
    : PsiReferenceBase<LuaIndexExpr>(element), LuaReference {

    private val content:LuaString = LuaString.getContent(id.text)

    override fun getRangeInElement(): TextRange {
        var start = id.node.startOffset - myElement.node.startOffset
        start += content.start
        return TextRange(start, start + content.length)
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val text = id.text
        val newText = text.substring(0, content.start) + newElementName + text.substring(content.end)
        val newId = LuaElementFactory.createLiteral(myElement.project, newText)
        id.replace(newId)
        return newId
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return myElement.manager.areElementsEquivalent(resolve(), element)
    }

    override fun resolve(): PsiElement? {
        return resolve(SearchContext(myElement.project))
    }

    override fun resolve(context: SearchContext): PsiElement? {
        val ref = resolve(myElement, content.value, context)
        if (ref != null) {
            if (ref.containingFile == myElement.containingFile) { //优化，不要去解析 Node Tree
                if (ref.node.textRange == myElement.node.textRange) {
                    return null//自己引用自己
                }
            }
        }
        return ref
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }
}
