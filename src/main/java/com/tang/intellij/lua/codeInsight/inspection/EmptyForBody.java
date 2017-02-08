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
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 空的for body
 * Created by TangZX on 2017/2/8.
 */
public class EmptyForBody extends LocalInspectionTool {

    private static Class[] invalidClasses = new Class[] {
            PsiWhiteSpace.class,
            PsiComment.class,
            LuaLocalFuncDef.class,
            LuaLocalDef.class
    };

    private static boolean isValid(PsiElement element) {
        for (Class invalidClass : invalidClasses) {
            if (invalidClass.isInstance(element)) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new LuaVisitor() {
            @Override
            public void visitForAStat(@NotNull LuaForAStat o) {
                LuaBlock block = PsiTreeUtil.findChildOfType(o, LuaBlock.class);
                if (!isValidBlock(block)) {
                    holder.registerProblem(o, "Empty for body", new Fix());
                }
            }

            @Override
            public void visitForBStat(@NotNull LuaForBStat o) {
                LuaBlock block = PsiTreeUtil.findChildOfType(o, LuaBlock.class);
                if (!isValidBlock(block)) {
                    holder.registerProblem(o, "Empty for body", new Fix());
                }
            }

            boolean isValidBlock(LuaBlock block) {
                if (block != null) {
                    PsiElement child = block.getFirstChild();
                    boolean hasValid = false;
                    while (child != null) {
                        if (isValid(child)) {
                            hasValid = true;
                            break;
                        }
                        child = child.getNextSibling();
                    }
                    return hasValid;
                }
                return true;
            }
        };
    }

    private class Fix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Remove empty for";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            problemDescriptor.getEndElement().delete();
        }
    }
}
