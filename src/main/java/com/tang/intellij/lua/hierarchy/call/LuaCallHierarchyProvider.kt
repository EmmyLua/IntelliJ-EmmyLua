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

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase
import com.intellij.ide.hierarchy.HierarchyBrowser
import com.intellij.ide.hierarchy.HierarchyProvider
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiElement

class LuaCallHierarchyProvider : HierarchyProvider {
    override fun getTarget(dataContext: DataContext): PsiElement? {
        val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return null
        val psiFile = dataContext.getData(CommonDataKeys.PSI_FILE) ?: return null
        val element = psiFile.findElementAt(editor.caretModel.offset)
        return LuaCallHierarchyUtil.getValidParentElement(element)
    }

    override fun createHierarchyBrowser(target: PsiElement): HierarchyBrowser {
        return LuaCallHierarchyBrowser(target)
    }

    override fun browserActivated(hierarchyBrowser: HierarchyBrowser) {
        (hierarchyBrowser as LuaCallHierarchyBrowser).changeView(CallHierarchyBrowserBase.CALLER_TYPE)
    }
}