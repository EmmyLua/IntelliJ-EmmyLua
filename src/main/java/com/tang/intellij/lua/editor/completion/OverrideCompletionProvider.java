package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import org.jetbrains.annotations.NotNull;

/**
 * override supper
 * Created by tangzx on 2016/12/25.
 */
public class OverrideCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement id = completionParameters.getPosition();
        LuaClassMethodDef methodDef = PsiTreeUtil.getParentOfType(id, LuaClassMethodDef.class);
        if (methodDef != null) {
            LuaType classType = methodDef.getClassType();
            if (classType != null) {
                LuaType sup = classType.getSuperClass();
                while (sup != null) {
                    sup.addMethodCompletions(completionParameters, completionResultSet);
                    sup = sup.getSuperClass();
                }
            }
        }
    }
}
