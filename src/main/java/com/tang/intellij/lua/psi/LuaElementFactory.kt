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

package com.tang.intellij.lua.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.comment.psi.LuaDocTagField
import com.tang.intellij.lua.lang.LuaLanguage

/**
 *
 * Created by TangZX on 2016/11/24.
 */
object LuaElementFactory {
    fun createFile(project: Project, content: String): LuaPsiFile {
        val name = "dummy.lua"
        return PsiFileFactory.getInstance(project).createFileFromText(name, LuaLanguage.INSTANCE, content) as LuaPsiFile
    }

    fun createIdentifier(project: Project, name: String): PsiElement {
        val content = "local $name = 0"
        val file = createFile(project, content)
        val def = PsiTreeUtil.findChildOfType(file, LuaNameDef::class.java)!!
        return def.firstChild
    }

    fun createLiteral(project: Project, value: String): LuaLiteralExpr {
        val content = "local a = $value"
        val file = createFile(project, content)
        return PsiTreeUtil.findChildOfType(file, LuaLiteralExpr::class.java)!!
    }

    fun createName(project: Project, name: String): PsiElement {
        val element = createWith(project, "$name = 1")
        return PsiTreeUtil.findChildOfType(element, LuaNameExpr::class.java)!!
    }

    fun newLine(project: Project): PsiElement {
        return createWith(project, "\n")
    }

    fun createWith(project: Project, code: String): PsiElement {
        val file = createFile(project, code)
        return file.firstChild
    }

    fun createDocIdentifier(project: Project, name: String): PsiElement {
        val element = createWith(project, "---@field $name string")
        val fieldDef = PsiTreeUtil.findChildOfType(element, LuaDocTagField::class.java)!!
        return fieldDef.id!!
    }

    fun createParamter(project: Project, name: String): PsiElement {
        val element = createWith(project, "local function($name)end")
        return PsiTreeUtil.findChildOfType(element, LuaParamNameDef::class.java)!!
    }
}
