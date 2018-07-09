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

package com.tang.intellij.lua.refactoring.rename

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.tang.intellij.lua.psi.LuaClassMethod
import com.tang.intellij.lua.psi.search.LuaOverridingMethodsSearch

/**
 *
 * Created by tangzx on 2017/3/29.
 */
class RenameLuaMethodProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(psiElement: PsiElement): Boolean {
        return psiElement is LuaClassMethod
    }

    override fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiElement, String>, scope: SearchScope) {
        val methodDef = element as LuaClassMethod

        /**
         * bug fix #167
         * TODO: optimize this issue solution
         * FIXME: the main reason is that `LuaNameExpr.infer` is suspended by `recursionGuard`
         * suspended stack : rename -> ... -> LuaNameExpr.infer -> ... -> rebuild stub index -> ... -> LuaNameExpr.infer (suspended)
         */
        FileDocumentManager.getInstance().saveAllDocuments()

        val search = LuaOverridingMethodsSearch.search(methodDef)
        search.forEach { methodDef1 -> allRenames[methodDef1] = newName }
    }
}
