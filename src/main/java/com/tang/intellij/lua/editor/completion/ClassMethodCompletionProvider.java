package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaCallExpr;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

import static com.tang.intellij.lua.editor.completion.LuaCompletionContributor.suggestWordsInFile;

/**
 *
 * Created by tangzx on 2016/12/25.
 */
public class ClassMethodCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement element = completionParameters.getPosition();//ID
        PsiElement parent = element.getParent();

        if (parent instanceof LuaCallExpr) {
            LuaCallExpr callExpr = (LuaCallExpr) parent;
            LuaTypeSet luaTypeSet = callExpr.guessPrefixType(new SearchContext(callExpr.getProject()));
            if (luaTypeSet != null) {
                luaTypeSet.getTypes().forEach(luaType -> luaType.addMethodCompletions(completionParameters, completionResultSet, false));
            }
        }
        //words in file
        suggestWordsInFile(completionParameters, processingContext, completionResultSet);
    }
}
