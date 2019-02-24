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
