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

package com.tang.intellij.lua.codeInsight.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.Constants;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/16.
 */
public class SimplifyLocalAssignment extends LocalInspectionTool {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new LuaVisitor() {
            @Override
            public void visitLocalDef(@NotNull LuaLocalDef o) {
                LuaExprList exprList = o.getExprList();
                if (exprList != null) {
                    List<LuaExpr> list = exprList.getExprList();
                    if (list.size() == 1) {
                        LuaExpr expr = list.get(0);
                        if (expr instanceof LuaLiteralExpr && Constants.WORD_NIL.equals(expr.getText())) {
                            holder.registerProblem(expr, "Local assignment can be simplified", new Fix());
                        }
                    }
                }
            }
        };
    }

    class Fix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Simplify local assignment";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            LuaLocalDef localDef = PsiTreeUtil.getParentOfType(problemDescriptor.getEndElement(), LuaLocalDef.class);
            assert localDef != null;
            LuaNameList nameList = localDef.getNameList();

            assert nameList != null;
            PsiElement assign = localDef.getAssign();
            assert assign != null;
            LuaExprList exprList = localDef.getExprList();
            assert exprList != null;
            localDef.deleteChildRange(assign, exprList);
        }
    }
}
