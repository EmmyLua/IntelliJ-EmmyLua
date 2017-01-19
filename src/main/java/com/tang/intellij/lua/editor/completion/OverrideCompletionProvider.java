/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaFuncBody;
import com.tang.intellij.lua.psi.LuaParamNameDef;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaClassMethodIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

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
            SearchContext context = new SearchContext(methodDef.getProject());
            LuaType classType = methodDef.getClassType(context);
            if (classType != null) {
                LuaType sup = classType.getSuperClass(context);
                addOverrideMethod(completionParameters, completionResultSet, sup);
            }
        }
    }

    private void addOverrideMethod(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, LuaType sup) {
        if (sup != null) {
            Project project = completionParameters.getOriginalFile().getProject();
            SearchContext context = new SearchContext(project);
            String clazzName = sup.getClassNameText();
            Collection<LuaClassMethodDef> list = LuaClassMethodIndex.getInstance().get(clazzName, project, new ProjectAndLibrariesScope(project));
            for (LuaClassMethodDef def : list) {
                String methodName = def.getName();
                if (methodName != null) {
                    LookupElementBuilder elementBuilder = LookupElementBuilder.create(def.getName())
                            .withIcon(LuaIcons.CLASS_METHOD)
                            .withInsertHandler(new OverrideInsertHandler(def.getFuncBody()))
                            .withTypeText("override " + clazzName);

                    completionResultSet.addElement(elementBuilder);
                }
            }

            sup = sup.getSuperClass(context);
            addOverrideMethod(completionParameters, completionResultSet, sup);
        }
    }

    static class OverrideInsertHandler extends FuncInsertHandler {
        OverrideInsertHandler(LuaFuncBody funcBody) {
            super(funcBody);
        }

        @Override
        protected Template createTemplate(TemplateManager manager, List<LuaParamNameDef> paramNameDefList) {
            Template template = super.createTemplate(manager, paramNameDefList);
            template.addEndVariable();
            template.addTextSegment("\nend");
            return template;
        }
    }
}
