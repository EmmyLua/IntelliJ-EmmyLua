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

package com.tang.intellij.lua.editor

import com.intellij.ide.actions.QualifiedNameProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.psi.LuaFileUtil
import com.tang.intellij.lua.psi.LuaPsiFile

/**
 *
 * Created by TangZX on 2016/12/30.
 */
class LuaQualifiedNameProvider : QualifiedNameProvider {
    override fun adjustElementToCopy(psiElement: PsiElement): PsiElement? {
        return null
    }

    override fun getQualifiedName(psiElement: PsiElement): String? {
        if (psiElement is LuaPsiFile) {
            val virtualFile = psiElement.virtualFile
            val project = psiElement.project
            return LuaFileUtil.asRequirePath(project, virtualFile)
        }
        return null
    }

    override fun qualifiedNameToElement(s: String, project: Project): PsiElement? {
        return null
    }

    override fun insertQualifiedName(s: String, psiElement: PsiElement?, editor: Editor, project: Project?) {

    }
}
