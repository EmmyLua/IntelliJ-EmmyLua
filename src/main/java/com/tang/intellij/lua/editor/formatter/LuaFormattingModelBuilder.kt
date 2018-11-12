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

package com.tang.intellij.lua.editor.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.tang.intellij.lua.editor.formatter.blocks.LuaScriptBlock
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.lang.LuaParserDefinition
import com.tang.intellij.lua.psi.LuaTypes.*

/**
 *
 * Created by tangzx on 2016/12/3.
 */
class LuaFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(element: PsiElement, settings: CodeStyleSettings): FormattingModel {
        val commonSettings = settings.getCommonSettings(LuaLanguage.INSTANCE)
        val luaSettings = settings.getCustomSettings(LuaCodeStyleSettings::class.java)
        val ctx = LuaFormatContext(commonSettings, luaSettings, createSpaceBuilder(settings))
        return FormattingModelProvider.createFormattingModelForPsiFile(element.containingFile,
                LuaScriptBlock(element,
                        Wrap.createWrap(WrapType.NONE, false),
                        Alignment.createAlignment(),
                        Indent.getNoneIndent(),
                        ctx),
                settings)
    }

    private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
        val luaCodeStyleSettings = settings.getCustomSettings(LuaCodeStyleSettings::class.java)
        val commonSettings = settings.getCommonSettings(LuaLanguage.INSTANCE)

        return SpacingBuilder(settings, LuaLanguage.INSTANCE)
                .before(END).lineBreakInCode()
                .after(DO).lineBreakInCode()
                .after(THEN).lineBreakInCode()
                .around(ELSE).lineBreakInCode()
                .before(ELSEIF).lineBreakInCode()
                .after(LOCAL).spaces(1) //local<SPACE>
                .around(COLON).none()
                .before(COMMA).spaces(if (commonSettings.SPACE_BEFORE_COMMA) 1 else 0)
                .after(COMMA).spaces(if (commonSettings.SPACE_AFTER_COMMA) 1 else 0) //,<SPACE>
                .between(LCURLY, TABLE_FIELD).spaces(1) // {<SPACE>1, 2 }
                .between(TABLE_FIELD, RCURLY).spaces(1) // { 1, 2<SPACE>}
                .before(TABLE_FIELD_SEP).none() // { 1<SPACE>, 2 }
                .after(TABLE_FIELD_SEP).spaces(if (luaCodeStyleSettings.SPACE_AFTER_TABLE_FIELD_SEP) 1 else 0) // { 1,<SPACE>2 }
                .before(BLOCK).blankLines(0)
                .afterInside(RPAREN, FUNC_BODY).lineBreakInCode()
                .between(FUNCTION, FUNC_BODY).none()
                .between(FUNCTION, NAME_DEF).spaces(1) //function<SPACE>name()
                .around(BINARY_OP).spaces(if (commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS) 1 else 0)
                .around(UNARY_OP).none()
                .around(ASSIGN).lineBreakOrForceSpace(false, commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS) // = 号两头不能换行
                .around(LuaParserDefinition.KEYWORD_TOKENS).spaces(1)
                .around(LBRACK).none() // [
                .before(RBRACK).none() // ]
                .after(LPAREN).none() // no spaces after (
                .before(RPAREN).none() // no spaces before )
                .before(SEMI).spaces(0)
    }

    override fun getRangeAffectingIndent(psiFile: PsiFile, i: Int, astNode: ASTNode): TextRange? {
        return null
    }
}
