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
import com.intellij.psi.PsiElementVisitor
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

class AssignTypeInspection : StrictInspection() {
    override fun buildVisitor(myHolder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
            object : LuaVisitor() {
                private fun inspectAssignee(assignee: LuaTypeGuessable, value: ITy, varianceFlags: Int, expressionElement: LuaExpr, targetAssignee: Boolean, context: SearchContext) {
                    val targetElement = if (targetAssignee) assignee else null

                    if (assignee is LuaIndexExpr) {
                        // Get owner class
                        val fieldOwnerType = assignee.guessParentType(context)

                        val idExpr = assignee.idExpr
                        val memberName = assignee.name

                        val assigneeMemberType = if (memberName != null) {
                            fieldOwnerType.guessMemberType(memberName, context)
                        } else {
                            idExpr?.let { fieldOwnerType.guessIndexerType(it.guessType(context), context) }
                        } ?: Ty.NIL

                        ProblemUtil.contravariantOf(assigneeMemberType, value, context, varianceFlags, targetElement, expressionElement) { targetElement, sourceElement, message, highlightType ->
                            myHolder.registerProblem(sourceElement, message, highlightType)
                            if (targetElement != null && targetElement != sourceElement) {
                                myHolder.registerProblem(targetElement, message, highlightType)
                            }
                        }
                    } else {
                        val variableType = assignee.guessType(context)
                        ProblemUtil.contravariantOf(variableType, value, context, varianceFlags, targetElement, expressionElement) { targetElement, sourceElement, message, highlightType ->
                            myHolder.registerProblem(sourceElement, message, highlightType)
                            if (targetElement != null && targetElement != sourceElement) {
                                myHolder.registerProblem(targetElement, message, highlightType)
                            }
                        }
                    }
                }

                fun inspectAssignment(assignees: List<LuaTypeGuessable>, expressions: List<LuaExpr>?) {
                    if (expressions == null || expressions.size == 0) {
                        return
                    }

                    val searchContext = SearchContext.get(expressions.first().project)
                    var assigneeIndex = 0
                    var variadicTy: ITy? = null

                    for (expressionIndex in 0 until expressions.size) {
                        val isLastExpression = expressionIndex == expressions.size - 1
                        val expression = expressions[expressionIndex]
                        val expressionType = if (isLastExpression) {
                            searchContext.withMultipleResults { expression.guessType(searchContext) }
                        } else {
                            searchContext.withIndex(0) { expression.guessType(searchContext) }
                        }
                        val varianceFlags = if (expression is LuaTableExpr) TyVarianceFlags.WIDEN_TABLES else 0
                        val multipleResults = expressionType as? TyMultipleResults
                        val values = if (multipleResults != null) multipleResults.list else listOf(expressionType)

                        for (valueIndex in 0 until values.size) {
                            val value = values[valueIndex]
                            val variadic = isLastExpression && valueIndex == values.size - 1 && multipleResults?.variadic == true

                            if (variadic) {
                                variadicTy = value
                            }

                            if (assigneeIndex >= assignees.size) {
                                if (!variadic) {
                                    for (i in expressionIndex until expressions.size) {
                                        myHolder.registerProblem(expressions[i], "Insufficient assignees, values will be discarded.", ProblemHighlightType.WEAK_WARNING)
                                    }
                                }
                                return
                            }

                            val assignee = assignees[assigneeIndex++]

                            if (assignees.size == 1 && (assignee.parent?.parent as? LuaCommentOwner)?.comment?.tagClass != null) {
                                if (value !is TyTable) {
                                    myHolder.registerProblem(expression, "Type mismatch. Required: 'table' Found: '%s'".format(value.displayName))
                                }
                            } else {
                                inspectAssignee(assignee, value, varianceFlags, expression, assignees.size > 1, searchContext)
                            }

                            if (!isLastExpression) {
                                break // Multiple values are only handled for the last expression
                            }
                        }
                    }

                    if (assigneeIndex < assignees.size) {
                        for (i in assigneeIndex until assignees.size) {
                            if (variadicTy != null) {
                                val assignee = assignees[assigneeIndex++]
                                inspectAssignee(assignee, variadicTy, 0, expressions.last(), assignees.size > 1, searchContext)
                            } else {
                                myHolder.registerProblem(assignees[i], "Too many assignees, will be assigned nil.")
                            }
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
