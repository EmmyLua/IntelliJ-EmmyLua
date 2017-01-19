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
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.tang.intellij.lua.psi.LuaArgs;
import com.tang.intellij.lua.psi.LuaCallExpr;
import com.tang.intellij.lua.psi.LuaTypes;
import com.tang.intellij.lua.psi.LuaVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 *
 * Created by tangzx on 2017/1/8.
 */
public class LuaStandardComplianceInspection extends LocalInspectionTool implements LuaTypes {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new LuaVisitor() {
            @Override
            public void visitCallExpr(@NotNull LuaCallExpr o) {
                // call(a, b, <<error>>)
                Optional.ofNullable(o.getArgs())
                        .map(LuaArgs::getExprList)
                        .map(PsiElement::getLastChild)
                        .map(e -> {
                            IElementType type = e.getNode().getElementType();
                            if (type == COMMA) {
                                holder.registerProblem(e, "Lua standard does not allow trailing comma", ProblemHighlightType.ERROR);
                            }
                            return type;
                        });
                super.visitCallExpr(o);
            }
        };
    }
}
