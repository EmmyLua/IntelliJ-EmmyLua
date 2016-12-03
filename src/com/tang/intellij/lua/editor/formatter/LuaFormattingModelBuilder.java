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
                new LuaScriptBlock(element.getNode(),
                        Wrap.createWrap(WrapType.NONE, false),
                        Alignment.createAlignment(),
                        Indent.getNoneIndent(),
                        createSpaceBuilder(settings)),
                settings);
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
        return new SpacingBuilder(settings, LuaLanguage.INSTANCE)
                .withinPair(DO, END).spaces(1, true)
                .after(LOCAL).spaces(1)
                .after(COMMA).spaces(1)
                .after(FUNCTION).spaces(1)
                .after(FUNC_NAME).none()
                .between(FUNCTION, NAME_DEF).spaces(1)
                .between(FUNCTION, FUNC_NAME).spaces(1)
                .between(FUNCTION, FUNC_BODY).none()
                .around(BINOP).spaces(1)
                .around(UNOP).spaces(1)
                .around(ASSIGN).spaces(1)
                .around(LuaSyntaxHighlighter.KEYWORD_TOKENS).spaces(1);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile psiFile, int i, ASTNode astNode) {
        return null;
    }
}
