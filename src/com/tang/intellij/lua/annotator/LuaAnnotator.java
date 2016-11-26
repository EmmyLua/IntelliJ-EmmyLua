package com.tang.intellij.lua.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import com.tang.intellij.lua.doc.psi.*;
import com.tang.intellij.lua.highlighting.LuaHighlightingData;
import com.tang.intellij.lua.psi.*;
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
        if (psiElement instanceof LuaDocElementType) {
            psiElement.accept(docVisitor);
        }
        else if (psiElement instanceof LuaPsiElement) {
            psiElement.accept(luaVisitor);
        }
        myHolder = null;
    }

    class LuaElementVisitor extends LuaVisitor {
        @Override
        public void visitLocalFuncDef(@NotNull LuaLocalFuncDef o) {
            LuaNameDef name = o.getNameDef();
            if (name != null) {
                Annotation annotation = myHolder.createInfoAnnotation(name, null);
                annotation.setTextAttributes(DefaultLanguageHighlighterColors.INSTANCE_FIELD);
            }
        }

        @Override
        public void visitGlobalFuncDef(@NotNull LuaGlobalFuncDef o) {
            LuaFuncName name = o.getFuncName();
            if (name != null) {
                Annotation annotation = myHolder.createInfoAnnotation(name, null);
                annotation.setTextAttributes(LuaHighlightingData.FIELD);
            }
        }
    }

    class LuaDocElementVisitor extends LuaDocVisitor {
        @Override
        public void visitTagName(@NotNull LuaDocTagName o) {
            Annotation annotation = myHolder.createInfoAnnotation(o, null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.DOC_COMMENT_TAG);
        }

        @Override
        public void visitClassName(@NotNull LuaDocClassName o) {
            Annotation annotation = myHolder.createInfoAnnotation(o, null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.CLASS_NAME);
        }

        @Override
        public void visitClassNameRef(@NotNull LuaDocClassNameRef o) {
            Annotation annotation = myHolder.createInfoAnnotation(o, null);
            annotation.setTextAttributes(DefaultLanguageHighlighterColors.CLASS_REFERENCE);
        }
    }
}
