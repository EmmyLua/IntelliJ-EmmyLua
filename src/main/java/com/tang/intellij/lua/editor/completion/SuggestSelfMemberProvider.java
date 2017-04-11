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
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaPsiImplUtil;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

/**
 * suggest self.xxx
 * Created by TangZX on 2017/4/11.
 */
public class SuggestSelfMemberProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
        PsiElement position = completionParameters.getPosition();
        LuaClassMethodDef methodDef = PsiTreeUtil.getParentOfType(position, LuaClassMethodDef.class);
        if (methodDef != null && !methodDef.isStatic()) {
            SearchContext searchContext = new SearchContext(position.getProject());
            LuaType type = methodDef.getClassType(searchContext);
            if (type != null) {
                type.processFields(completionParameters, searchContext, (curType, field) -> {
                    String fieldName = field.getFieldName();
                    if (fieldName != null) {
                        LookupElementBuilder elementBuilder = LookupElementBuilder.create("self." + fieldName)
                                .withIcon(LuaIcons.CLASS_FIELD)
                                .withTypeText(curType.getDisplayName());
                        if (curType == type)
                            elementBuilder = elementBuilder.bold();
                        completionResultSet.addElement(elementBuilder);
                    }
                });

                type.processMethods(completionParameters, searchContext, (curType, def) -> {
                    String methodName = def.getName();
                    if (methodName != null) {
                        LuaPsiImplUtil.processOptional(def.getParams(), (signature, mask) -> {
                            LookupElementBuilder elementBuilder = LookupElementBuilder.create(methodName + signature, "self:" + methodName)
                                    .withIcon(LuaIcons.CLASS_METHOD)
                                    .withTypeText(curType.getDisplayName())
                                    .withInsertHandler(new FuncInsertHandler(def).withMask(mask))
                                    .withTailText(signature);
                            if (curType == type)
                                elementBuilder = elementBuilder.bold();
                            completionResultSet.addElement(elementBuilder);
                        });
                    }
                });
            }
        }
    }
}