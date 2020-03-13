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

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

class AssignTypeInspection : StrictInspection() {
    override fun buildVisitor(myHolder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
            object : LuaVisitor() {
                fun inspectAssignment(assignees: List<LuaTypeGuessable>, expressions: List<LuaExpr>?) {
                    if (expressions == null || expressions.size == 0) {
                        return
                    }

                    val searchContext = SearchContext.get(expressions.first().project)
                    var assigneeIndex = 0

                    for (expressionIndex in 0 until expressions.size) {
                        val expression = expressions[expressionIndex]
                        val expressionType = expression.guessType(searchContext)
                        val varianceFlags = if (expression is LuaTableExpr) TyVarianceFlags.WIDEN_TABLES else 0
                        val values = if (expressionType is TyTuple) expressionType.list else listOf(expressionType)
                        val isLastExpression = expressionIndex == expressions.size - 1

                        for (value in values) {
                            if (assigneeIndex >= assignees.size) {
                                for (i in expressionIndex until expressions.size) {
                                    myHolder.registerProblem(expressions[i], "Insufficient assignees, values will be discarded.", ProblemHighlightType.WEAK_WARNING)
                                }
                                return
                            }

                            val assignee = assignees[assigneeIndex++]

                            if (assignee is LuaIndexExpr) {
                                // Get owner class
                                val fieldOwnerType = assignee.guessParentType(searchContext)
                                val fieldOwnerClass = if (fieldOwnerType is TyGeneric) fieldOwnerType.base else fieldOwnerType

                                if (fieldOwnerClass is TyClass) {
                                    val name = assignee.name ?: ""
                                    val fieldType = fieldOwnerClass.findMemberType(name, searchContext) ?: Ty.NIL
                                    ProblemUtil.contravariantOf(fieldType, value, searchContext, varianceFlags, expression) { element, message, highlightType ->
                                        myHolder.registerProblem(element, message, highlightType)
                                    }
                                }
                            } else if (assignees.size == 1 && (assignee.parent?.parent as? LuaCommentOwner)?.comment?.tagClass != null) {
                                if (value !is TyTable) {
                                    myHolder.registerProblem(expression, "Type mismatch. Required: 'table' Found: '%s'".format(value.displayName))
                                }
                            } else {
                                val variableType = assignee.guessType(searchContext)
                                ProblemUtil.contravariantOf(variableType, value, searchContext, varianceFlags, expression) { element, message, highlightType ->
                                    myHolder.registerProblem(element, message, highlightType)
                                }
                            }

                            if (!isLastExpression) {
                                break // Multiple (tuple) values are only handled for the last expression
                            }
                        }
                    }

                    if (assigneeIndex < assignees.size) {
                        for (i in assigneeIndex until assignees.size) {
                            myHolder.registerProblem(assignees[i], "Too many assignees, will be assigned nil.")
                        }
                    }
                }

                override fun visitAssignStat(o: LuaAssignStat) {
                    super.visitAssignStat(o)
                    inspectAssignment(o.varExprList.exprList, o.valueExprList?.exprList)
                }

                override fun visitLocalDef(o: LuaLocalDef) {
                    super.visitLocalDef(o)
                    val nameList = o.nameList

                    if (nameList != null) {
                        inspectAssignment(nameList.nameDefList, o.exprList?.exprList)
                    }
                }
            }
}
