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

package com.tang.intellij.lua.refactoring.rename;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.introduce.inplace.InplaceVariableIntroducer;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.tang.intellij.lua.psi.LuaElementFactory;
import com.tang.intellij.lua.psi.LuaExpr;
import com.tang.intellij.lua.psi.LuaNameDef;
import com.tang.intellij.lua.psi.LuaStatement;
import com.tang.intellij.lua.refactoring.LuaRefactoringUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 *
 * Created by tangzx on 2017/4/25.
 */
public class LuaIntroduceVarHandler implements RefactoringActionHandler {

    class IntroduceOperation {
        public Project project;
        public Editor editor;
        public PsiFile file;
        private boolean replaceAll;
        private List<PsiElement> occurrences;
        public String name = "var";
        private List<PsiElement> newOccurrences;
        private LuaNameDef var;
        private PsiElement element;

        IntroduceOperation(Project project, Editor editor, PsiFile psiFile) {
            this.project = project;
            this.editor = editor;
            file = psiFile;
        }

        void setReplaceAll(boolean replaceAll) {
            this.replaceAll = replaceAll;
        }

        boolean isReplaceAll() {
            return replaceAll;
        }

        void setOccurrences(List<PsiElement> occurrences) {
            this.occurrences = occurrences;
        }

        List<PsiElement> getOccurrences() {
            return occurrences;
        }

        void setNewOccurrences(List<PsiElement> newOccurrences) {
            this.newOccurrences = newOccurrences;
        }

        PsiElement[] getNewOccurrences() {
            return newOccurrences.toArray(new PsiElement[newOccurrences.size()]);
        }

        public PsiNamedElement getVar() {
            return var;
        }

        public void setVar(LuaNameDef var) {
            this.var = var;
        }

        public PsiElement getElement() {
            return element;
        }

        public void setElement(PsiElement element) {
            this.element = element;
        }
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile, DataContext dataContext) {

    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiElement[] psiElements, DataContext dataContext) {

    }

    public void invoke(Project project, Editor editor, LuaExpr expr) {
        IntroduceOperation operation = new IntroduceOperation(project, editor, expr.getContainingFile());
        operation.setElement(expr);
        operation.setOccurrences(getOccurrences(expr));
        OccurrencesChooser.simpleChooser(editor).showChooser(expr, operation.getOccurrences(), new Pass<OccurrencesChooser.ReplaceChoice>() {
            @Override
            public void pass(OccurrencesChooser.ReplaceChoice choice) {
                operation.setReplaceAll(choice == OccurrencesChooser.ReplaceChoice.ALL);
                WriteCommandAction.runWriteCommandAction(operation.project, () -> {
                    performReplace(operation);
                });
                performInplaceIntroduce(operation);
            }
        });
    }

    private List<PsiElement> getOccurrences(LuaExpr expr) {
        return LuaRefactoringUtil.getOccurrences(expr, expr.getContainingFile());
    }

    private static PsiElement findAnchor(List<PsiElement> occurrences) {
        PsiElement anchor = occurrences.get(0);
        next:
        do {
            final LuaStatement statement = PsiTreeUtil.getParentOfType(anchor, LuaStatement.class);
            if (statement != null) {
                final PsiElement parent = statement.getParent();
                for (PsiElement element : occurrences) {
                    if (!PsiTreeUtil.isAncestor(parent, element, true)) {
                        anchor = statement;
                        continue next;
                    }
                }
            }
            return statement;
        }
        while (true);
    }

    private boolean isInline(PsiElement commonParent, IntroduceOperation operation) {
        return (commonParent instanceof LuaStatement || commonParent == operation.element) &&
                (!operation.isReplaceAll() || operation.getOccurrences().size() == 1);
    }

    private void performReplace(IntroduceOperation operation) {
        if (!operation.isReplaceAll())
            operation.setOccurrences(Collections.singletonList(operation.element));

        PsiElement commonParent = PsiTreeUtil.findCommonParent(operation.getOccurrences());
        if (commonParent != null) {
            List<PsiElement> newOccurrences = new SmartList<>();
            PsiElement localDef = LuaElementFactory.createWith(operation.project, "local var = " + operation.getElement().getText());

            if (isInline(commonParent, operation)) {
                localDef = operation.element.replace(localDef);
                LuaNameDef nameDef = PsiTreeUtil.findChildOfType(localDef, LuaNameDef.class);
                assert nameDef != null;
                operation.editor.getCaretModel().moveToOffset(nameDef.getTextOffset());
            } else {
                PsiElement anchor = findAnchor(operation.getOccurrences());
                commonParent = anchor.getParent();
                localDef = commonParent.addBefore(localDef, anchor);
                commonParent.addAfter(LuaElementFactory.newLine(operation.project), localDef);
                for (PsiElement occ : operation.getOccurrences()) {
                    PsiElement identifier = LuaElementFactory.createName(operation.project, operation.name);
                    identifier = occ.replace(identifier);
                    newOccurrences.add(identifier);
                }
            }

            operation.setNewOccurrences(newOccurrences);
            localDef = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(localDef);
            LuaNameDef nameDef = PsiTreeUtil.findChildOfType(localDef, LuaNameDef.class);
            operation.setVar(nameDef);
        }
    }

    private void performInplaceIntroduce(IntroduceOperation operation) {
        new LuaIntroduce(operation).performInplaceRefactoring(null);
    }

    private class LuaIntroduce extends InplaceVariableIntroducer<PsiElement> {
        LuaIntroduce(IntroduceOperation operation) {
            super(operation.getVar(), operation.editor, operation.project, "Introduce Variable", operation.getNewOccurrences(), null);
        }

        @Nullable
        @Override
        protected PsiElement checkLocalScope() {
            PsiFile currentFile = PsiDocumentManager.getInstance(this.myProject).getPsiFile(this.myEditor.getDocument());
            return currentFile != null?currentFile:super.checkLocalScope();
        }
    }
 }
