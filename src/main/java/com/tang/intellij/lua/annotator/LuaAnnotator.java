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

package com.tang.intellij.lua.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.tang.intellij.lua.Constants;
import com.tang.intellij.lua.comment.psi.*;
import com.tang.intellij.lua.highlighting.LuaHighlightingData;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.search.SearchContext;
import org.jetbrains.annotations.NotNull;

/**
 * LuaAnnotator
 * Created by TangZX on 2016/11/22.
 */
public class LuaAnnotator extends LuaVisitor implements Annotator {
    private AnnotationHolder myHolder;
    private LuaElementVisitor luaVisitor = new LuaElementVisitor();
    private LuaDocElementVisitor docVisitor = new LuaDocElementVisitor();

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        myHolder = annotationHolder;
        if (psiElement instanceof LuaDocPsiElement) {
            psiElement.accept(docVisitor);
        }
        else if (psiElement instanceof LuaPsiElement) {
            psiElement.accept(luaVisitor);
        }
        myHolder = null;
    }

    class LuaElementVisitor extends LuaVisitor {

        @Override
        public void visitUncompleted(@NotNull LuaUncompleted o) {
            myHolder.createErrorAnnotation(o, "Uncompleted");
        }

        @Override
        public void visitLocalFuncDef(@NotNull LuaLocalFuncDef o) {
            PsiElement name = o.getNameIdentifier();

            if (name != null) {
                Annotation annotation = myHolder.createInfoAnnotation(name, null);
                annotation.setTextAttributes(LuaHighlightingData.LOCAL_VAR);
            }
        }

        @Override
        public void visitTableField(@NotNull LuaTableField o) {
            super.visitTableField(o);
            PsiElement id = o.getId();
            if (id != null) {
                Annotation annotation = myHolder.createInfoAnnotation(id, null);
                annotation.setTextAttributes(LuaHighlightingData.TABLE_FIELD);
            }
        }

        @Override
        public void visitGlobalFuncDef(@NotNull LuaGlobalFuncDef o) {
            PsiElement name = o.getNameIdentifier();
            if (name != null) {
                Annotation annotation = myHolder.createInfoAnnotation(name, null);
                annotation.setTextAttributes(LuaHighlightingData.GLOBAL_FUNCTION);
            }
        }

        @Override
        public void visitClassMethodName(@NotNull LuaClassMethodName o) {
            Annotation annotation = myHolder.createInfoAnnotation(o.getId(), null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.INSTANCE_METHOD);
        }

        @Override
        public void visitParamNameDef(@NotNull LuaParamNameDef o) {
            if (o.textMatches(Constants.WORD_UNDERLINE))
                return;

            Query<PsiReference> search = ReferencesSearch.search(o, o.getUseScope());
            if (search.findFirst() == null) {
                myHolder.createInfoAnnotation(o, "Unused parameter : " + o.getText());
                //annotation.setTextAttributes(CodeInsightColors.WEAK_WARNING_ATTRIBUTES);
            } else {
                Annotation annotation = myHolder.createInfoAnnotation(o, null);
                annotation.setTextAttributes(LuaHighlightingData.PARAMETER);
            }
        }

        @Override
        public void visitNameRef(@NotNull LuaNameRef o) {
            PsiElement id = o.getFirstChild();
            if (id == null)
                return;

            //up value
            boolean upValue = LuaPsiResolveUtil.isUpValue(o, new SearchContext(o.getProject()));
            if (upValue) {
                Annotation annotation = myHolder.createInfoAnnotation(o, null);
                annotation.setTextAttributes(LuaHighlightingData.UP_VALUE);
            }

            PsiElement res = o.resolve(new SearchContext(o.getProject()));
            if (res != null) {
                PsiFile containingFile = res.getContainingFile();
                PsiDirectory directory = containingFile.getContainingDirectory();
                if (directory.getName().equals("std")) {
                    Annotation annotation = myHolder.createInfoAnnotation(o, null);
                    annotation.setTextAttributes(LuaHighlightingData.STD);
                }
            }

            if (res instanceof LuaParamNameDef) {
                Annotation annotation = myHolder.createInfoAnnotation(o, null);
                annotation.setTextAttributes(LuaHighlightingData.PARAMETER);
            } else if (res instanceof LuaGlobalFuncDef) {
                Annotation annotation = myHolder.createInfoAnnotation(o, null);
                annotation.setTextAttributes(LuaHighlightingData.GLOBAL_FUNCTION);
            } else if (res instanceof LuaNameDef || res instanceof LuaLocalFuncDef) { //Local
                if (id.textMatches(Constants.WORD_SELF)) {
                    Annotation annotation = myHolder.createInfoAnnotation(o, null);
                    annotation.setTextAttributes(LuaHighlightingData.SELF);
                } else {
                    Annotation annotation = myHolder.createInfoAnnotation(o, null);
                    annotation.setTextAttributes(LuaHighlightingData.LOCAL_VAR);
                }
            } else/* if (res instanceof LuaNameRef) */{ // 未知的，视为Global
                Annotation annotation = myHolder.createInfoAnnotation(o, null);
                annotation.setTextAttributes(LuaHighlightingData.GLOBAL_VAR);
            }
        }
    }

    class LuaDocElementVisitor extends LuaDocVisitor {
        @Override
        public void visitClassDef(@NotNull LuaDocClassDef o) {
            super.visitClassDef(o);
            Annotation annotation = myHolder.createInfoAnnotation(o.getId(), null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.CLASS_NAME);
        }

        @Override
        public void visitClassNameRef(@NotNull LuaDocClassNameRef o) {
            Annotation annotation = myHolder.createInfoAnnotation(o, null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.CLASS_REFERENCE);
        }

        @Override
        public void visitFieldDef(@NotNull LuaDocFieldDef o) {
            super.visitFieldDef(o);
            PsiElement id = o.getNameIdentifier();
            if (id != null) {
                Annotation annotation = myHolder.createInfoAnnotation(id, null);
                annotation.setTextAttributes(LuaHighlightingData.DOC_COMMENT_TAG_VALUE);
            }
        }

        @Override
        public void visitParamNameRef(@NotNull LuaDocParamNameRef o) {
            Annotation annotation = myHolder.createInfoAnnotation(o, null);
            annotation.setTextAttributes(LuaHighlightingData.DOC_COMMENT_TAG_VALUE);
        }
    }
}
