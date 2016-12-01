package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.psi.index.LuaGlobalFuncIndex;
import com.tang.intellij.lua.psi.stub.elements.LuaGlobalFuncDefStubElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

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

                            //提示方法
                            if (classDef != null) {
                                String clazzName = classDef.getClassName().getText();
                                Collection<LuaGlobalFuncDef> list = LuaGlobalFuncIndex.getInstance().get(clazzName, project, new ProjectAndLibrariesScope(project));
                                for (LuaGlobalFuncDef def : list) {
                                    completionResultSet.addElement(LookupElementBuilder.create(def.getFuncName().getId().getText()));
                                }
                            }

                            //提示属性
                        }
                    }
                }
            }
        });

        //提示全局函数
        extend(CompletionType.BASIC, psiElement().inside(LuaFile.class), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                Project project = completionParameters.getOriginalFile().getProject();
                Collection<LuaGlobalFuncDef> list = LuaGlobalFuncIndex.getInstance().get(LuaGlobalFuncDefStubElementType.NON_PREFIX_GLOBAL_FUNC, project, new ProjectAndLibrariesScope(project));
                for (LuaGlobalFuncDef def : list) {
                    completionResultSet.addElement(LookupElementBuilder.create(def.getFuncName().getText()));
                }
            }
        });
    }
}
