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

package com.tang.intellij.lua.usages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.comment.psi.LuaDocTagField
import com.tang.intellij.lua.lang.LuaParserDefinition
import com.tang.intellij.lua.lexer.LuaLexerAdapter
import com.tang.intellij.lua.psi.*

/**
 *
 * Created by TangZX on 2016/11/24.
 */
class LuaFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner? {
        return DefaultWordsScanner(LuaLexerAdapter(),
                TokenSet.create(),
                LuaParserDefinition.COMMENTS,
                LuaParserDefinition.STRINGS)
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is PsiNamedElement
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(psiElement: PsiElement): String {
        if (psiElement is LuaDocTagClass)
            return "Class"
        if (psiElement is LuaDocTagField)
            return "Class Field"
        if (psiElement is LuaFuncDef)
            return "Global Function"
        if (psiElement is LuaLocalFuncDef)
            return "Local Function"
        if (psiElement is LuaClassMethodDef)
            return "Class Function"
        if (psiElement is LuaParamNameDef)
            return "Param"
        return if (psiElement is LuaTableField) "Table Field" else "Name"
    }

    override fun getDescriptiveName(psiElement: PsiElement): String {
        if (psiElement is PsiNamedElement) {
            val name = psiElement.name
            if (name != null)
                return name
        }
        return ""
    }

    override fun getNodeText(psiElement: PsiElement, b: Boolean): String {
        return getDescriptiveName(psiElement)
    }
}
