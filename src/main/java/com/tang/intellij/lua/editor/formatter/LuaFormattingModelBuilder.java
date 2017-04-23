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

package com.tang.intellij.lua.editor.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.tang.intellij.lua.highlighting.LuaSyntaxHighlighter;
import com.tang.intellij.lua.lang.LuaLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.tang.intellij.lua.psi.LuaTypes.*;

/**
 *
 * Created by tangzx on 2016/12/3.
 */
public class LuaFormattingModelBuilder implements FormattingModelBuilder {
    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(),
                new LuaScriptBlock(null,
                        element.getNode(),
                        Wrap.createWrap(WrapType.NONE, false),
                        Alignment.createAlignment(),
                        Indent.getNoneIndent(),
                        createSpaceBuilder(settings)),
                settings);
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
        final LuaCodeStyleSettings luaCodeStyleSettings = settings.getCustomSettings(LuaCodeStyleSettings.class);

        return new SpacingBuilder(settings, LuaLanguage.INSTANCE)
                .before(END).lineBreakInCode()
                .after(DO).lineBreakInCode()
                .after(THEN).lineBreakInCode()
                .after(LOCAL).spaces(1) //local<SPACE>
                .after(COMMA).spaces(settings.SPACE_AFTER_COMMA ? 1 : 0) //,<SPACE>
                .between(LCURLY, TABLE_FIELD).spaces(1) // {<SPACE>1, 2 }
                .between(TABLE_FIELD, RCURLY).spaces(1) // { 1, 2<SPACE>}
                .after(TABLE_FIELD_SEP).spaces(luaCodeStyleSettings.SPACE_AFTER_TABLE_FIELD_SEP ? 1 : 0) // { 1,<SPACE>2 }
                .before(BLOCK).blankLines(0)
                .afterInside(RPAREN, FUNC_BODY).lineBreakInCode()
                .between(FUNCTION, FUNC_BODY).none()
                .between(FUNCTION, NAME_DEF).spaces(1) //function<SPACE>name()
                .between(VALUE_EXPR, COMMA).lineBreakOrForceSpace(false, false)
                .around(BINARY_OP).spaces(settings.SPACE_AROUND_ASSIGNMENT_OPERATORS ? 1 : 0)
                .around(UNARY_OP).none()
                .around(ASSIGN).lineBreakOrForceSpace(false, settings.SPACE_AROUND_ASSIGNMENT_OPERATORS) // = 号两头不能换行
                .around(LuaSyntaxHighlighter.KEYWORD_TOKENS).spaces(1)
                .before(COMMA).spaces(settings.SPACE_BEFORE_COMMA ? 1 : 0)
                .before(SEMI).spaces(0);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile psiFile, int i, ASTNode astNode) {
        return null;
    }
}
