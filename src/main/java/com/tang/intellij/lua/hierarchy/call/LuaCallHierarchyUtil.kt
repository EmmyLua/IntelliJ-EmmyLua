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

package com.tang.intellij.lua.hierarchy.call

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch
import com.tang.intellij.lua.psi.*

object LuaCallHierarchyUtil {
    fun isValidElement(element: PsiElement?): Boolean {
        return element is LuaClassMethodDef || element is LuaFuncDef || element is LuaLocalFuncDef
    }

    fun getValidParentElement(element: PsiElement?): PsiElement? {
        var curElement = element
        while (curElement != null) {
            if (isValidElement(curElement)) return curElement
            if (curElement is PsiFile) break
            curElement = curElement.parent
        }
        return null
    }

    fun getCallers(element: PsiElement): List<PsiElement> {
        return ReferencesSearch.search(element)
                .mapNotNull { LuaCallHierarchyUtil.getValidParentElement(it.element) }
    }

    fun getCallees(element: PsiElement): List<PsiElement> {
        val callees = mutableListOf<PsiElement>()
        val visitor = object : LuaRecursiveVisitor() {
            override fun visitCallExpr(o: LuaCallExpr) {
                o.expr.reference?.resolve()?.takeIf { LuaCallHierarchyUtil.isValidElement(it) }?.let {
                    callees.add(it)
                }
            }
        }
        visitor.visitElement(element)
        return callees
    }

}