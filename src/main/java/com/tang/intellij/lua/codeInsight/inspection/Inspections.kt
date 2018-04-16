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

package com.tang.intellij.lua.codeInsight.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.tang.intellij.lua.psi.*

class UnreachableStatement : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
            object : LuaVisitor() {
                override fun visitStatement(o: LuaStatement) {
                    var sibling = o.prevSibling
                    var found = false
                    while (sibling != null && !found) {
                        if (sibling is LuaReturnStat) {
                            found = true
                            holder.registerProblem(o, "Unreachable statement", object : LocalQuickFix {
                                override fun getName() = "Remove unreachable statement"

                                override fun getFamilyName() = name

                                override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
                                    o.delete()
                                }

                            })
                        }
                        sibling = sibling.prevSibling
                    }

                    if (!found) super.visitStatement(o)
                }
            }
}

/**
 * local value = myTable[0]
 */
class ArrayIndexZero : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : LuaVisitor() {
            override fun visitIndexExpr(o: LuaIndexExpr) {
                o.acceptChildren(object : PsiElementVisitor() {
                    override fun visitElement(element: PsiElement?) {
                        if (element is LuaLiteralExpr && element.text == "0") {
                            holder.registerProblem(element, "0 index", object : LocalQuickFix {
                                override fun getName() = "Replace with index 1"

                                override fun getFamilyName() = name

                                override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
                                    val newLiteral = LuaElementFactory.createLiteral(o.project, "1")
                                    element.replace(newLiteral)
                                }
                            })
                        }
                    }
                })

                super.visitIndexExpr(o)
            }
        }
    }
}