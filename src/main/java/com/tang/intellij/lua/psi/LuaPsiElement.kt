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

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.lang.LuaLanguageLevel
import com.tang.intellij.lua.project.LuaSettings

/**
 * LuaPsiElement
 * Created by TangZX on 2016/11/22.
 */
interface LuaPsiElement : NavigatablePsiElement

val LuaPsiElement.moduleName: String? get() {
    val file = containingFile
    if (file is LuaPsiFile)
        return file.moduleName
    return null
}

val LuaPsiElement.languageLevel: LuaLanguageLevel get() {
    val file = containingFile
    if (file is LuaPsiFile)
        return file.languageLevel
    return LuaSettings.instance.languageLevel
}

val PsiElement.realContext: PsiElement get() {
    val file = containingFile
    if (file is LuaExprCodeFragment)
        return file.context ?: this
    return this
}