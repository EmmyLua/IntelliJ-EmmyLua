/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tang.intellij.lua.codeInsight.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import com.tang.intellij.lua.Constants;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * Created by TangZX on 2017/2/8.
 */
public class RemoveUnusedLocal extends LocalInspectionTool {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new LuaVisitor() {

            @Override
            public void visitLocalDef(@NotNull LuaLocalDef o) {
                LuaNameList nameList = o.getNameList();
                if (nameList != null) {
                    List<LuaNameDef> nameDefList = nameList.getNameDefList();
                    if (nameDefList.size() == 1) {
                        LuaNameDef nameDef = nameDefList.get(0);

                        if (nameDef.textMatches(Constants.WORD_UNDERLINE))
                            return;

                        Query<PsiReference> search = ReferencesSearch.search(nameDef, nameDef.getUseScope());
                        if (search.findFirst() == null) {
                            holder.registerProblem(o, "Remove unused local", new Fix());
                        }
                    }
                }
            }

            @Override
            public void visitLocalFuncDef(@NotNull LuaLocalFuncDef o) {
                PsiElement name = o.getNameIdentifier();

                if (name != null) {
                    Query<PsiReference> search = ReferencesSearch.search(o, o.getUseScope());
                    if (search.findFirst() == null) {
                        holder.registerProblem(o, "Remove unused local function", new Fix());
                    }
                }
            }
        };
    }

    private class Fix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Remove it";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            PsiElement element = problemDescriptor.getEndElement();
            element.delete();
        }
    }
}
