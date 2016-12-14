package com.tang.intellij.lua.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.HashSet;
import com.tang.intellij.lua.highlighting.LuaSyntaxHighlighter;
import com.tang.intellij.lua.lang.LuaIcons;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.lang.type.LuaTypeSet;
import com.tang.intellij.lua.psi.*;
import com.tang.intellij.lua.stubs.index.LuaGlobalFieldIndex;
import com.tang.intellij.lua.stubs.index.LuaGlobalFuncIndex;
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
    private static final PsiElementPattern.Capture<PsiElement> SHOW_FIELD = psiElement().afterLeaf(
            psiElement().withText(".").withParent(LuaIndexExpr.class));
    private static final PsiElementPattern.Capture<PsiElement> IN_COMMENT = psiElement().inside(psiElement().withElementType(LuaTypes.DOC_COMMENT));

    public LuaCompletionContributor() {

        //提示方法
        extend(CompletionType.BASIC, SHOW_CLASS_METHOD, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                PsiElement element = completionParameters.getOriginalFile().findElementAt(completionParameters.getOffset() - 1);

                if (element != null) {
                    LuaCallExpr callExpr = (LuaCallExpr) element.getParent();
                    LuaTypeSet luaTypeSet = callExpr.guessPrefixType();
                    if (luaTypeSet != null) {
                        luaTypeSet.getTypes().forEach(luaType -> luaType.addMethodCompletions(completionParameters, completionResultSet));
                    }
                }
                //words in file
                suggestWordsInFile(completionParameters, processingContext, completionResultSet);
            }
        });

        //提示属性
        extend(CompletionType.BASIC, SHOW_FIELD, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                PsiElement element = completionParameters.getOriginalFile().findElementAt(completionParameters.getOffset() - 1);

                if (element != null) {
                    LuaIndexExpr indexExpr = (LuaIndexExpr) element.getParent();
                    LuaTypeSet prefixTypeSet = indexExpr.guessPrefixType();
                    if (prefixTypeSet != null) {
                        prefixTypeSet.getTypes().forEach(luaType -> luaType.addFieldCompletions(completionParameters, completionResultSet));
                    }
                }
                //words in file
                suggestWordsInFile(completionParameters, processingContext, completionResultSet);
            }
        });

        //提示全局函数,local变量,local函数
        extend(CompletionType.BASIC, psiElement().inside(LuaFile.class).andNot(SHOW_CLASS_METHOD).andNot(SHOW_FIELD).andNot(IN_COMMENT), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                //local
                PsiElement cur = completionParameters.getOriginalFile().findElementAt(completionParameters.getOffset() - 1);
                LuaPsiTreeUtil.walkUpLocalNameDef(cur, nameDef -> {
                    LookupElementBuilder elementBuilder = LookupElementBuilder.create(nameDef.getText())
                            .withIcon(LuaIcons.LOCAL_VAR);
                    completionResultSet.addElement(elementBuilder);
                    return  true;
                });
                LuaPsiTreeUtil.walkUpLocalFuncNameDef(cur, nameDef -> {
                    LookupElementBuilder elementBuilder = LookupElementBuilder.create(nameDef.getText())
                            .withIcon(LuaIcons.LOCAL_FUNCTION);
                    completionResultSet.addElement(elementBuilder);
                    return true;
                });

                //global functions
                Project project = completionParameters.getOriginalFile().getProject();
                Collection<String> list = LuaGlobalFuncIndex.getInstance().getAllKeys(project);
                for (String name : list) {
                    LookupElementBuilder elementBuilder = LookupElementBuilder.create(name)
                            .withTypeText("Global Func")
                            .withIcon(LuaIcons.GLOBAL_FUNCTION);
                    completionResultSet.addElement(elementBuilder);
                }

                //global fields
                Collection<String> allGlobalFieldNames = LuaGlobalFieldIndex.getInstance().getAllKeys(project);
                for (String name : allGlobalFieldNames) {
                    completionResultSet.addElement(LookupElementBuilder.create(name).withIcon(LuaIcons.GLOBAL_FIELD));
                }

                //key words
                TokenSet keywords = TokenSet.orSet(LuaSyntaxHighlighter.KEYWORD_TOKENS, LuaSyntaxHighlighter.PRIMITIVE_TYPE_SET);
                keywords = TokenSet.orSet(TokenSet.create(LuaTypes.SELF), keywords);
                for (IElementType keyWordToken : keywords.getTypes()) {
                    completionResultSet.addElement(LookupElementBuilder.create(keyWordToken));
                }

                //words in file
                suggestWordsInFile(completionParameters, processingContext, completionResultSet);
            }
        });
    }

    private static void suggestWordsInFile(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        HashSet<String> wordsInFileSet = new HashSet<>();
        PsiFile file = completionParameters.getOriginalFile();
        file.acceptChildren(new LuaVisitor() {
            @Override
            public void visitPsiElement(@NotNull LuaPsiElement o) {
                o.acceptChildren(this);
            }

            @Override
            public void visitElement(PsiElement element) {
                if (element.getNode().getElementType() == LuaTypes.ID && element.getTextLength() > 2) {
                    wordsInFileSet.add(element.getText());
                }
                super.visitElement(element);
            }
        });

        for (String s : wordsInFileSet) {
            completionResultSet.addElement(PrioritizedLookupElement.withPriority(LookupElementBuilder
                            .create(s)
                            .withIcon(LuaIcons.WORD)
                            .withTypeText("Word In File")
                    , -1));
        }
    }

    private static void suggestKeywords(PsiElement position) {

        GeneratedParserUtilBase.CompletionState state = new GeneratedParserUtilBase.CompletionState(8) {
            @Override
            public String convertItem(Object o) {
                if (o instanceof LuaTokenType) {
                    LuaTokenType tokenType = (LuaTokenType) o;
                    return tokenType.toString();
                }
                // we do not have other keywords
                return o instanceof String? (String)o : null;
            }
        };

        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(position.getProject());
        PsiFile file = psiFileFactory.createFileFromText("a.lua", LuaLanguage.INSTANCE, "local ", true, false);
        file.putUserData(GeneratedParserUtilBase.COMPLETION_STATE_KEY, state);
        TreeUtil.ensureParsed(file.getNode());

        System.out.println("---");
    }
}
