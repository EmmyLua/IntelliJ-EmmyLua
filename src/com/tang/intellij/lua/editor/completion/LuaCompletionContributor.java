package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
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

    private static final PsiElementPattern.Capture<PsiElement> SHOW_CLASS_METHOD = psiElement().afterLeaf(
            psiElement().withText(":").withParent(LuaCallExpr.class));

    public LuaCompletionContributor() {
        extend(CompletionType.BASIC, SHOW_CLASS_METHOD, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                Project project = completionParameters.getOriginalFile().getProject();

                PsiElement element = completionParameters.getOriginalFile().findElementAt(completionParameters.getOffset() - 1);
                if (element != null) {
                    LuaCallExpr callExpr = (LuaCallExpr) element.getParent();
                    LuaNameRef ref = callExpr.getNameRef();
                    if (ref != null) {
                        PsiElement resolve = ref.resolve();
                        if (resolve instanceof LuaTypeResolvable) {
                            LuaTypeResolvable typeResolvable = (LuaTypeResolvable) resolve;
                            LuaTypeSet luaTypeSet = typeResolvable.resolveType();

                            //提示方法
                            if (luaTypeSet != null) {
                                luaTypeSet.getTypes().forEach(luaType -> {
                                    String clazzName = luaType.getClassNameText();
                                    Collection<LuaGlobalFuncDef> list = LuaGlobalFuncIndex.getInstance().get(clazzName, project, new ProjectAndLibrariesScope(project));
                                    for (LuaGlobalFuncDef def : list) {
                                        LookupElementBuilder elementBuilder = LookupElementBuilder.create(def.getFuncName().getId().getText())
                                                .withIcon(AllIcons.Nodes.Method)
                                                .withTypeText(clazzName);

                                        completionResultSet.addElement(elementBuilder);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });

        //提示全局函数,local变量,local函数
        extend(CompletionType.BASIC, psiElement().inside(LuaFile.class).andNot(SHOW_CLASS_METHOD), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                //local
                PsiElement cur = completionParameters.getOriginalFile().findElementAt(completionParameters.getOffset());
                LuaPsiTreeUtil.walkUpLocalNameDef(cur, nameDef -> {
                    LookupElementBuilder elementBuilder = LookupElementBuilder.create(nameDef.getText())
                            .withIcon(AllIcons.Nodes.Variable);
                    completionResultSet.addElement(elementBuilder);
                    return  true;
                });
                LuaPsiTreeUtil.walkUpLocalFuncDef(cur, nameDef -> {
                    LookupElementBuilder elementBuilder = LookupElementBuilder.create(nameDef.getText())
                            .withIcon(AllIcons.Nodes.Method);
                    completionResultSet.addElement(elementBuilder);
                    return true;
                });

                //global functions
                Project project = completionParameters.getOriginalFile().getProject();
                Collection<LuaGlobalFuncDef> list = LuaGlobalFuncIndex.getInstance().get(LuaGlobalFuncDefStubElementType.NON_PREFIX_GLOBAL_FUNC, project, new ProjectAndLibrariesScope(project));
                for (LuaGlobalFuncDef def : list) {
                    LuaFuncName funcName = def.getFuncName();
                    if (funcName != null) {
                        String text = funcName.getText();
                        LookupElementBuilder elementBuilder = LookupElementBuilder.create(text)
                                .withTypeText("Global Func")
                                .withIcon(AllIcons.Nodes.Function);
                        completionResultSet.addElement(elementBuilder);
                    }
                }
            }
        });
    }
}
