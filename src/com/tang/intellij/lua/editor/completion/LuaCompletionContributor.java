package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaNameRef;
import com.tang.intellij.lua.psi.LuaTypeResolvable;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 *
 * Created by tangzx on 2016/11/27.
 */
public class LuaCompletionContributor extends CompletionContributor {

    private static final PsiElementPattern.Capture<PsiElement> AFTER_DOT = psiElement().afterLeaf(
            psiElement().withText(".").withParent(LuaIndexExpr.class));

    public LuaCompletionContributor() {
        extend(CompletionType.BASIC, AFTER_DOT, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                Project project = completionParameters.getOriginalFile().getProject();

                //global functions
                //Collection<String> all = LuaGlobalFuncIndex.getInstance().getAllKeys(completionParameters.getOriginalFile().getProject());
                //all.forEach(name -> completionResultSet.addElement(LookupElementBuilder.create(name)));

                //class
                //Collection<String> allClasses = LuaClassIndex.getInstance().getAllKeys(project);
                //allClasses.forEach(className -> completionResultSet.addElement(LookupElementBuilder.create(className)));

                PsiElement element = completionParameters.getOriginalFile().findElementAt(completionParameters.getOffset() - 1);
                if (element != null) {
                    LuaIndexExpr indexExpr = (LuaIndexExpr) element.getParent();
                    PsiElement prev = indexExpr.getPrevSibling();
                    if (prev instanceof LuaNameRef) {
                        LuaNameRef ref = (LuaNameRef) prev;
                        PsiElement resolve = ref.resolve();
                        if (resolve instanceof LuaTypeResolvable) {
                            LuaTypeResolvable typeResolvable = (LuaTypeResolvable) resolve;
                            LuaDocClassDef classDef = typeResolvable.resolveType();
                            if (classDef != null) {
                                completionResultSet.addElement(LookupElementBuilder.create(classDef.getClassName()));
                            }
                        }
                    }
                }
            }
        });
    }

}
