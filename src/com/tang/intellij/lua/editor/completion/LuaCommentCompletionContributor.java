package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.ElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import com.tang.intellij.lua.doc.LuaCommentUtil;
import com.tang.intellij.lua.doc.psi.LuaDocPsiElement;
import com.tang.intellij.lua.doc.psi.LuaDocTypes;
import com.tang.intellij.lua.highlighting.LuaSyntaxHighlighter;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.psi.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 *
 * Created by tangzx on 2016/12/2.
 */
public class LuaCommentCompletionContributor extends CompletionContributor {

    private static final PsiElementPattern.Capture<PsiElement> SHOW_DOC_TAG = psiElement().afterLeaf(
            psiElement().withText("@")
                    .afterSiblingSkipping(psiElement().withElementType(ElementType.WHITE_SPACE), psiElement().withElementType(LuaDocTypes.DASHES))
    );

    private static final PsiElementPattern.Capture<PsiElement> AFTER_PARAM =  psiElement().afterLeaf(
            psiElement().withElementType(LuaDocTypes.TAG_PARAM)
    );

    private static final  PsiElementPattern.Capture<PsiElement> SHOW_CLASS_AFTER_SHARP =  psiElement().afterLeaf(
            psiElement().withText("#").inside(psiElement().withElementType(LuaTypes.DOC_COMMENT))
    );

    private static final  PsiElementPattern.Capture<PsiElement> SHOW_CLASS_AFTER_COMMA =  psiElement().afterLeaf(
            psiElement().withText(",").inside(psiElement().withElementType(LuaTypes.DOC_COMMENT))
    );

    private static final  PsiElementPattern.Capture<PsiElement> SHOW_ACCESS_MODIFIER =  psiElement().afterLeaf(
            psiElement().withElementType(LuaDocTypes.FIELD).inside(psiElement().withElementType(LuaTypes.DOC_COMMENT))
    );

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
                            List<LuaParDef> parDefList = body.getParDefList();
                            for (LuaParDef def : parDefList) {
                                completionResultSet.addElement(
                                        LookupElementBuilder.create(def.getText())
                                                .withIcon(AllIcons.Nodes.Variable)
                                );
                            }
                        }
                    }
                }
            }
        });

        extend(CompletionType.BASIC, psiElement().andOr(SHOW_CLASS_AFTER_SHARP, SHOW_CLASS_AFTER_COMMA), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                Project project = completionParameters.getPosition().getProject();
                Collection<String> collection = LuaClassIndex.getInstance().getAllKeys(project);
                collection.forEach(className -> {
                    completionResultSet.addElement(LookupElementBuilder.create(className).withIcon(AllIcons.Nodes.Class));
                });
            }
        });

        extend(CompletionType.BASIC, SHOW_ACCESS_MODIFIER, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                completionResultSet.addElement(LookupElementBuilder.create("protected"));
                completionResultSet.addElement(LookupElementBuilder.create("public"));
            }
        });
    }

}
