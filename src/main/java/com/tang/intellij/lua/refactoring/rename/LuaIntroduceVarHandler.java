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
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.jetbrains.annotations.NotNull;

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
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile, DataContext dataContext) {

    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiElement[] psiElements, DataContext dataContext) {

    }

    public void invoke(Project project, Editor editor, LuaExpr expr) {
        IntroduceOperation operation = new IntroduceOperation(project, editor, expr.getContainingFile());
        List<PsiElement> list = new SmartList<>();
        list.add(expr);
        operation.setOccurrences(list);
        OccurrencesChooser.simpleChooser(editor).showChooser(expr, list, new Pass<OccurrencesChooser.ReplaceChoice>() {
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

    private void performReplace(IntroduceOperation operation) {
        PsiElement commonParent = PsiTreeUtil.findCommonParent(operation.getOccurrences());
        if (commonParent != null) {
            while (!(commonParent instanceof PsiFile)) {
                commonParent = commonParent.getParent();
            }

            List<PsiElement> newOccurrences = new SmartList<>();
            PsiElement anchor = commonParent.getFirstChild();
            PsiElement localDef = LuaElementFactory.createWith(operation.project, "local var = 0");
            localDef = commonParent.addBefore(localDef, anchor);

            LuaNameDef nameDef = PsiTreeUtil.findChildOfType(localDef, LuaNameDef.class);
            operation.setVar(nameDef);

            for (PsiElement occ : operation.getOccurrences()) {
                PsiElement identifier = LuaElementFactory.createName(operation.project, operation.name);
                identifier = occ.replace(identifier);
                newOccurrences.add(identifier);
            }
            operation.setNewOccurrences(newOccurrences);
        }
        PsiDocumentManager.getInstance(operation.project).doPostponedOperationsAndUnblockDocument(operation.editor.getDocument());
    }

    private void performInplaceIntroduce(IntroduceOperation operation) {
        new LuaIntroduce(operation).performInplaceRefactoring(null);
    }

    private class LuaIntroduce extends InplaceVariableIntroducer<PsiElement> {
        LuaIntroduce(IntroduceOperation operation) {
            super(operation.getVar(), operation.editor, operation.project, "Introduce Variable", operation.getNewOccurrences(), null);
        }
    }
 }
