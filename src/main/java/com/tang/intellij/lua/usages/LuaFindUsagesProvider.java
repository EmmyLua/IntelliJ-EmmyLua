package com.tang.intellij.lua.usages;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import com.tang.intellij.lua.comment.psi.LuaDocClassName;
import com.tang.intellij.lua.comment.psi.LuaDocFieldNameDef;
import com.tang.intellij.lua.lang.LuaParserDefinition;
import com.tang.intellij.lua.lexer.LuaLexerAdapter;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/11/24.
 */
public class LuaFindUsagesProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new LuaLexerAdapter(),
                TokenSet.create(),
                LuaParserDefinition.COMMENTS,
                LuaParserDefinition.STRINGS);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement psiElement) {
        if (psiElement instanceof LuaDocClassName)
            return "Class";
        if (psiElement instanceof LuaDocFieldNameDef)
            return "Class Field";
        if (psiElement instanceof LuaGlobalFuncDef)
            return "Global Function";
        if (psiElement instanceof LuaLocalFuncDef)
            return "Local Function";
        if (psiElement instanceof LuaClassMethodDef)
            return "Class Function";
        if (psiElement instanceof LuaParamNameDef)
            return "Param";
        if (psiElement instanceof LuaTableField)
            return "Table Field";
        return "Name";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiNamedElement) {
            PsiNamedElement namedElement = (PsiNamedElement) psiElement;
            String name = namedElement.getName();
            if (name != null)
                return name;
        }
        return "";
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement psiElement, boolean b) {
        return getDescriptiveName(psiElement);
    }
}
