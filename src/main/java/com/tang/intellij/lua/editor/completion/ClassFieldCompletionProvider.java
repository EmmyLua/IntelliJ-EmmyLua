package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import org.jetbrains.annotations.NotNull;

import static com.tang.intellij.lua.editor.completion.LuaCompletionContributor.suggestWordsInFile;

/**
 *
 * Created by tangzx on 2016/12/25.
 */
public class ClassFieldCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement element = completionParameters.getPosition();
        PsiElement parent = element.getParent();

        if (parent instanceof LuaIndexExpr) {
            LuaIndexExpr indexExpr = (LuaIndexExpr) parent;
            LuaTypeSet prefixTypeSet = indexExpr.guessPrefixType();
            if (prefixTypeSet != null) {
                prefixTypeSet.getTypes().forEach(luaType -> luaType.addFieldCompletions(completionParameters, completionResultSet));
            }
        }
        //words in file
        suggestWordsInFile(completionParameters, processingContext, completionResultSet);
    }
}
