/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tang.intellij.lua.codeInsight.inspection;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 空的for body
 * Created by TangZX on 2017/2/8.
 */
public class EmptyBody extends EmptyBodyBase {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new LuaVisitor() {
            @Override
            public void visitForAStat(@NotNull LuaForAStat o) {
                checkBlock(o, holder, LuaTypes.FOR, "Empty for body", "Remove empty for");
            }

            @Override
            public void visitForBStat(@NotNull LuaForBStat o) {
                checkBlock(o, holder, LuaTypes.FOR, "Empty for body", "Remove empty for");
            }

            @Override
            public void visitDoStat(@NotNull LuaDoStat o) {
                checkBlock(o, holder, LuaTypes.DO, "Empty do body", "Remove empty do");
            }

            @Override
            public void visitWhileStat(@NotNull LuaWhileStat o) {
                checkBlock(o, holder, LuaTypes.WHILE, "Empty while body", "Remove empty do");
            }
        };
    }

    private void checkBlock(@NotNull PsiElement o, @NotNull ProblemsHolder holder, IElementType highlightType, String message, String familyName) {
        LuaBlock block = PsiTreeUtil.findChildOfType(o, LuaBlock.class);
        if (!isValidBlock(block)) {
            ASTNode forNode = o.getNode().findChildByType(highlightType);
            assert forNode != null;

            PsiElement forElement = forNode.getPsi();
            int offset = forElement.getNode().getStartOffset() - o.getNode().getStartOffset();
            TextRange textRange = new TextRange(offset, offset + forElement.getTextLength());
            holder.registerProblem(o, textRange, message, new Fix(familyName));
        }
    }

    private class Fix implements LocalQuickFix {

        private String familyName;

        Fix(String familyName) {
            this.familyName = familyName;
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return familyName;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            problemDescriptor.getEndElement().delete();
        }
    }
}
