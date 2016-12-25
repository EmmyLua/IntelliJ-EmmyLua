package com.tang.intellij.lua.usages;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.tang.intellij.lua.lexer.LuaLexerAdapter;
import com.tang.intellij.lua.psi.LuaGlobalFuncNameDef;
import com.tang.intellij.lua.psi.LuaName;
import com.tang.intellij.lua.psi.LuaParamNameDef;
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
                TokenSet.create(),
                TokenSet.create());
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof LuaName;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement psiElement) {
        if (psiElement instanceof LuaGlobalFuncNameDef)
            return "Global Function";
        if (psiElement instanceof LuaParamNameDef)
            return "Param";
        return "Name";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement psiElement) {
        if (psiElement instanceof LuaName)
            return ((LuaName)psiElement).getName();
        else
            return "";
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement psiElement, boolean b) {
        return ((LuaName)psiElement).getName();
    }
}
