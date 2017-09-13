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

package com.tang.intellij.lua.refactoring

import com.intellij.codeInsight.PsiEquivalenceUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.psi.LuaVisitor
import java.util.*

/**
 * RefactoringUtil
 * Created by tangzx on 2017/4/30.
 */
object LuaRefactoringUtil {
    fun getOccurrences(pattern: PsiElement, context: PsiElement?): List<PsiElement> {
        if (context == null) {
            return emptyList()
        }
        val occurrences = ArrayList<PsiElement>()
        val visitor = object : LuaVisitor() {
            override fun visitElement(element: PsiElement) {
                if (PsiEquivalenceUtil.areElementsEquivalent(element, pattern)) {
                    occurrences.add(element)
                    return
                }
                element.acceptChildren(this)
            }
        }
        context.acceptChildren(visitor)
        return occurrences
    }

    fun isLuaIdentifier(name: String): Boolean {
        return StringUtil.isJavaIdentifier(name)
    }
}
