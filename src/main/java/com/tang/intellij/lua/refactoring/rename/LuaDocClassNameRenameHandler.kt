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

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.refactoring.LuaRefactoringUtil

class LuaDocClassRenameInputValidator : RenameInputValidator {
    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext): Boolean {
        return LuaRefactoringUtil.isLuaTypeIdentifier(newName)
    }

    override fun getPattern(): ElementPattern<out PsiElement> {
        return PlatformPatterns.psiElement(LuaDocTagClass::class.java)
    }
}