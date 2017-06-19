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

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.comment.LuaCommentUtil;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.comment.psi.api.LuaComment;
import com.tang.intellij.lua.highlighting.LuaSyntaxHighlighter;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.type.LuaType;
import com.tang.intellij.lua.psi.LuaCommentOwner;
import com.tang.intellij.lua.psi.LuaFuncBody;
import com.tang.intellij.lua.psi.LuaFuncBodyOwner;
import com.tang.intellij.lua.psi.LuaParamNameDef;
import com.tang.intellij.lua.search.SearchContext;
import com.tang.intellij.lua.stubs.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * doc 相关代码完成
 * Created by tangzx on 2016/12/2.
 */
public class LuaCommentCompletionContributor extends CompletionContributor {

    // 在 @ 之后提示 param class type ...
    private static final PsiElementPattern.Capture<PsiElement> SHOW_DOC_TAG = psiElement().afterLeaf(
            psiElement().withText("@")
                    .afterSiblingSkipping(psiElement().withElementType(TokenType.WHITE_SPACE), psiElement().withElementType(LuaDocTypes.DASHES))
    );

    // 在 @param 之后提示方法的参数
    private static final PsiElementPattern.Capture<PsiElement> AFTER_PARAM = psiElement().withParent(LuaDocParamNameRef.class);

    // 在 @param 之后提示 optional
    private static final PsiElementPattern.Capture<PsiElement> SHOW_OPTIONAL = psiElement().afterLeaf(
            psiElement(LuaDocTypes.TAG_PARAM));

    // 在 extends 之后提示类型
    private static final PsiElementPattern.Capture<PsiElement> SHOW_CLASS = psiElement().withParent(LuaDocClassNameRef.class);

    // 在 @field 之后提示 public / protected
    private static final PsiElementPattern.Capture<PsiElement> SHOW_ACCESS_MODIFIER = psiElement().afterLeaf(
            psiElement().withElementType(LuaDocTypes.FIELD)
    );

    private static final PsiElementPattern.Capture<PsiElement> SHOW_FIELD = psiElement(LuaDocTypes.ID).inside(LuaDocFieldDef.class);

    public LuaCommentCompletionContributor() {
        extend(CompletionType.BASIC, SHOW_DOC_TAG, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                TokenSet set = LuaSyntaxHighlighter.DOC_KEYWORD_TOKENS;
                for (IElementType type : set.getTypes()) {
                    completionResultSet.addElement(LookupElementBuilder.create(type));
                }
            }
        });

        extend(CompletionType.BASIC, SHOW_OPTIONAL, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                completionResultSet.addElement(LookupElementBuilder.create("optional"));
            }
        });

        extend(CompletionType.BASIC, AFTER_PARAM, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                PsiElement element = completionParameters.getOriginalFile().findElementAt(completionParameters.getOffset() - 1);
                if (element != null && !(element instanceof LuaDocPsiElement))
                    element = element.getParent();

                if (element instanceof LuaDocPsiElement) {
                    LuaCommentOwner owner = LuaCommentUtil.findOwner((LuaDocPsiElement) element);
                    if (owner instanceof LuaFuncBodyOwner) {
                        LuaFuncBodyOwner bodyOwner = (LuaFuncBodyOwner) owner;
                        LuaFuncBody body = bodyOwner.getFuncBody();
                        if (body != null) {
                            List<LuaParamNameDef> parDefList = body.getParamNameDefList();
                            for (LuaParamNameDef def : parDefList) {
                                completionResultSet.addElement(
                                        LookupElementBuilder.create(def.getText())
                                                .withIcon(LuaIcons.PARAMETER)
                                );
                            }
                        }
                    }
                }
            }
        });

        extend(CompletionType.BASIC, SHOW_CLASS, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                Project project = completionParameters.getPosition().getProject();
                LuaClassIndex.getInstance().processAllKeys(project, className -> {
                    if (completionResultSet.getPrefixMatcher().prefixMatches(className)) {
                        completionResultSet.addElement(LookupElementBuilder.create(className).withIcon(LuaIcons.CLASS));
                    }
                    return true;
                });
                completionResultSet.stopHere();
            }
        });

        extend(CompletionType.BASIC, SHOW_ACCESS_MODIFIER, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                completionResultSet.addElement(LookupElementBuilder.create("protected"));
                completionResultSet.addElement(LookupElementBuilder.create("public"));
            }
        });

        // 属性提示
        extend(CompletionType.BASIC, SHOW_FIELD, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                PsiElement position = completionParameters.getPosition();
                LuaComment comment = PsiTreeUtil.getParentOfType(position, LuaComment.class);
                LuaDocClassDef classDef = PsiTreeUtil.findChildOfType(comment, LuaDocClassDef.class);
                if (classDef != null) {
                    LuaType classType = classDef.getClassType();
                    if (classType != null) {
                        classType.processFields(new SearchContext(classDef.getProject()), (curType, field) -> {
                            completionResultSet.addElement(LookupElementBuilder.create(field.getFieldName()));
                        });
                    }
                }
            }
        });
    }

}
