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

package com.tang.intellij.lua.refactoring.inline

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.lang.LuaLanguage

class LuaInlineActionHandler : InlineActionHandler() {
    override fun inlineElement(project: Project, editor: Editor, psiElement: PsiElement) {
        
    }

    override fun isEnabledForLanguage(language: Language): Boolean {
        return language == LuaLanguage.INSTANCE
    }

    override fun canInlineElement(element: PsiElement): Boolean {
        return true
    }
}