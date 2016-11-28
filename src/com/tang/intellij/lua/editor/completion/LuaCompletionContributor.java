package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.doc.psi.LuaDocClassDef;
import com.tang.intellij.lua.doc.psi.api.LuaComment;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
import com.tang.intellij.lua.psi.index.LuaGlobalFuncIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 *
 * Created by tangzx on 2016/11/27.
 */
public class LuaCompletionContributor extends CompletionContributor {

    private static final PsiElementPattern.Capture<PsiElement> AFTER_DOT = psiElement().afterLeaf(".", ":");

    public LuaCompletionContributor() {
        extend(CompletionType.BASIC, AFTER_DOT, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                Project project = completionParameters.getOriginalFile().getProject();
                Collection<LuaGlobalFuncDef> defs = LuaGlobalFuncIndex.getInstance().get("s", project, new ProjectAndLibrariesScope(project));
                for (LuaGlobalFuncDef def : defs) {
                    LuaComment comment = def.getComment();
                    if (comment != null) {
                        LuaDocClassDef classDec = PsiTreeUtil.findChildOfType(comment, LuaDocClassDef.class);
                        if (classDec != null) {
                            completionResultSet.addElement(LookupElementBuilder.create(classDec.getText()));
                        }
                    }
                }

                Collection<String> all = LuaGlobalFuncIndex.getInstance().getAllKeys(completionParameters.getOriginalFile().getProject());
                all.forEach(name -> completionResultSet.addElement(LookupElementBuilder.create(name)));
            }
        });
    }

}
