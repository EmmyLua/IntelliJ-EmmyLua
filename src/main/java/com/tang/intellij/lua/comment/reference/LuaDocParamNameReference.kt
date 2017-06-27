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

package com.tang.intellij.lua.comment.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocParamNameRef
import com.tang.intellij.lua.psi.*

/**
 * 函数参数引用
 * Created by tangzx on 2016/11/25.
 */
class LuaDocParamNameReference(element: LuaDocParamNameRef) : PsiReferenceBase<LuaDocParamNameRef>(element) {

    override fun getRangeInElement(): TextRange {
        return TextRange(0, myElement.textLength)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return myElement.manager.areElementsEquivalent(element, resolve())
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val id = LuaElementFactory.createIdentifier(myElement.project, newElementName)
        myElement.firstChild.replace(id)
        return id
    }

    override fun resolve(): PsiElement? {
        val owner = LuaCommentUtil.findOwner(myElement)

        if (owner != null) {
            val name = myElement.text
            var target:PsiElement? = null
            owner.accept(object :LuaVisitor() {
                override fun visitPsiElement(o: LuaPsiElement) {
                    if (o is LuaParametersOwner) {
                        target = findParamWithName(o.paramNameDefList, name)
                    }

                    target ?: o.acceptChildren(this)
                }
            })
            return target
        }
        return null
    }

    private fun findParamWithName(defList: List<LuaParamNameDef>?, str: String): PsiElement? {
        return defList?.firstOrNull { it.text == str }
    }

    override fun getVariants(): Array<Any?> {
        return arrayOfNulls(0)
    }
}
