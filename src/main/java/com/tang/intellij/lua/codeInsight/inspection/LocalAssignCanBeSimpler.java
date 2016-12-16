package com.tang.intellij.lua.codeInsight.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by TangZX on 2016/12/16.
 */
public class LocalAssignCanBeSimpler extends LocalInspectionTool {
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
                        if (expr instanceof LuaValueExpr && "nil".equals(expr.getText())) {
                            holder.registerProblem(o, "Local Assign can be simpler", new Fix());
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
            return "Delete nil";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            LuaLocalDef localDef = (LuaLocalDef) problemDescriptor.getEndElement();
            LuaNameList nameList = localDef.getNameList();

            assert nameList != null;
            List<PsiElement> deleteList = new ArrayList<>();
            PsiElement next = nameList.getNextSibling();
            while (next != null) {
                deleteList.add(next);
                next = next.getNextSibling();
            }
            for (int i = deleteList.size() - 1; i >= 0; i--) {
                deleteList.get(i).delete();
            }
        }
    }
}
