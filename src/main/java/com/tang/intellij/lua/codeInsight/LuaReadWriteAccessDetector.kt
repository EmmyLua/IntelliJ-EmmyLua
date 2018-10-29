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

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.comment.psi.LuaDocClassNameRef
import com.tang.intellij.lua.comment.psi.LuaDocTagField
import com.tang.intellij.lua.comment.psi.LuaDocParamNameRef
import com.tang.intellij.lua.psi.*

class LuaReadWriteAccessDetector : ReadWriteAccessDetector() {
    override fun getReferenceAccess(element: PsiElement, reference: PsiReference): Access {
        return getExpressionAccess(reference.element)
    }

    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        return element is LuaPsiElement
    }

    override fun getExpressionAccess(element: PsiElement): Access {
        return when (element) {
            is LuaNameDef -> Access.Write
            is LuaExpr -> {
                if (element.assignStat != null)
                    Access.Write
                else Access.Read
            }
            is LuaIndexExpr -> Access.Read
            is LuaDocTagClass -> Access.Write
            is LuaDocClassNameRef -> Access.Read
            is LuaDocParamNameRef -> Access.Read
            is LuaDocTagField -> Access.Write
            else -> return Access.ReadWrite
        }
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        return element is LuaPsiElement
    }
}