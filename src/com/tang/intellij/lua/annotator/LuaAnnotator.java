package com.tang.intellij.lua.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.doc.psi.LuaDocClassName;
import com.tang.intellij.lua.doc.psi.LuaDocClassNameRef;
import com.tang.intellij.lua.doc.psi.LuaDocTagName;
import com.tang.intellij.lua.highlighting.LuaHighlightingData;
import com.tang.intellij.lua.psi.LuaFuncName;
import com.tang.intellij.lua.psi.LuaGlobalFuncDef;
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
        else if (psiElement instanceof LuaDocClassName) {
            Annotation annotation = annotationHolder.createInfoAnnotation(psiElement, null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.CLASS_NAME);
        }
        else if (psiElement instanceof LuaDocClassNameRef) {
            Annotation annotation = annotationHolder.createInfoAnnotation(psiElement, null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.CLASS_REFERENCE);
        }
        else if (psiElement instanceof LuaGlobalFuncDef) {
            LuaGlobalFuncDef def = (LuaGlobalFuncDef)psiElement;
            LuaFuncName name = def.getFuncName();
            if (name != null) {
                Annotation annotation = annotationHolder.createInfoAnnotation(name, null);
                annotation.setTextAttributes(LuaHighlightingData.FIELD);
            }
        }
    }
}
