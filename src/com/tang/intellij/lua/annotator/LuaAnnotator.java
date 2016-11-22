package com.tang.intellij.lua.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.doc.psi.LuaDocTagName;
import org.jetbrains.annotations.NotNull;

/**
 * LuaAnnotator
 * Created by TangZX on 2016/11/22.
 */
public class LuaAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof LuaDocTagName) {
            Annotation annotation = annotationHolder.createInfoAnnotation(psiElement, null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.DOC_COMMENT_TAG);
        }
    }
}
