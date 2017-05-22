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
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.LuaClassField;
import com.tang.intellij.lua.psi.LuaClassMethodDef;
import com.tang.intellij.lua.psi.LuaIndexExpr;
import com.tang.intellij.lua.psi.LuaPsiImplUtil;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

import static com.tang.intellij.lua.editor.completion.LuaCompletionContributor.suggestWordsInFile;

/**
 *
 * Created by tangzx on 2016/12/25.
 */
public class ClassMemberCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement element = completionParameters.getPosition();
        PsiElement parent = element.getParent();

        if (parent instanceof LuaIndexExpr) {
            LuaIndexExpr indexExpr = (LuaIndexExpr) parent;
            LuaTypeSet prefixTypeSet = indexExpr.guessPrefixType(new SearchContext(indexExpr.getProject()));
            if (prefixTypeSet != null) {
                if (indexExpr.getColon() != null) {
                    prefixTypeSet.getTypes().forEach(luaType -> {
                        SearchContext context = new SearchContext(indexExpr.getProject());
                        luaType.initAliasName(context);
                        luaType.processMethods(context, (curType, def) -> {
                            String className = curType.getDisplayName();
                            addMethod(completionResultSet, curType == luaType, false, className, def);
                        });
                    });
                } else {
                    prefixTypeSet.getTypes().forEach(luaType -> {
                        SearchContext context = new SearchContext(indexExpr.getProject());
                        luaType.initAliasName(context);
                        luaType.processMethods(context, (curType, def) -> {
                            String className = curType.getDisplayName();
                            addMethod(completionResultSet, curType == luaType, true, className, def);
                        });
                        luaType.processFields(context, (curType, field) -> {
                            String className = curType.getDisplayName();
                            addField(completionResultSet, curType == luaType, className, field);
                        });
                        luaType.processStaticMethods(context, (curType, def) -> {
                            addStaticMethod(completionResultSet, curType == luaType, curType.getDisplayName(), def);
                        });
                    });
                }
            }
        }
        //words in file
        suggestWordsInFile(completionParameters, completionResultSet);
    }

    private void addField(@NotNull CompletionResultSet completionResultSet, boolean bold, String clazzName, LuaClassField field) {
        String name = field.getFieldName();
        if (name != null && completionResultSet.getPrefixMatcher().prefixMatches(name)) {
            LuaFieldLookupElement elementBuilder = new LuaFieldLookupElement(name, field, bold);
            completionResultSet.addElement(elementBuilder);
        }
    }

    private void addMethod(@NotNull CompletionResultSet completionResultSet, boolean bold, boolean useAsField, String clazzName, LuaClassMethodDef def) {
        String methodName = def.getName();
        if (methodName != null && completionResultSet.getPrefixMatcher().prefixMatches(methodName)) {
            if (useAsField) {
                LookupElementBuilder elementBuilder = LookupElementBuilder.create(methodName)
                        .withIcon(LuaIcons.CLASS_METHOD)
                        .withTailText(def.getParamSignature());
                if (bold)
                    elementBuilder = elementBuilder.bold();
                completionResultSet.addElement(elementBuilder);
            } else {
                LuaPsiImplUtil.processOptional(def.getParams(), (signature, mask) -> {
                    LuaMethodLookupElement elementBuilder = new LuaMethodLookupElement(methodName, signature, bold, def);
                    elementBuilder.setHandler(new FuncInsertHandler(def).withMask(mask));
                    completionResultSet.addElement(elementBuilder);
                });
            }
        }
    }

    private void addStaticMethod(@NotNull CompletionResultSet completionResultSet, boolean bold, String clazzName, LuaClassMethodDef def) {
        String methodName = def.getName();
        if (methodName != null && completionResultSet.getPrefixMatcher().prefixMatches(methodName)) {
            LuaPsiImplUtil.processOptional(def.getParams(), (signature, mask) -> {
                LuaMethodLookupElement elementBuilder = new LuaMethodLookupElement(methodName, signature, bold, def);
                elementBuilder.setHandler(new FuncInsertHandler(def).withMask(mask));
                elementBuilder.setItemTextUnderlined(true);
                completionResultSet.addElement(elementBuilder);
            });
        }
    }
}
