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
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*

class MatchFunctionSignatureInspection : StrictInspection() {
    data class ConcreteTypeInfo(val param: LuaExpr, val ty: ITy)
    override fun buildVisitor(myHolder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
            object : LuaVisitor() {
                override fun visitIndexExpr(o: LuaIndexExpr) {
                    super.visitIndexExpr(o)
                    val id = o.id
                    if (id != null) {
                        if (o.parent is LuaCallExpr && o.colon != null) {
                            // Guess parent types
                            val context = SearchContext.get(o.project)
                            o.exprList.forEach { expr ->
                                if (expr.guessType(context) == Ty.NIL) {
                                    // If parent type is nil add error
                                    myHolder.registerProblem(expr, "Trying to index a nil type.")
                                }
                            }
                        }
                    }
                }

                override fun visitCallExpr(o: LuaCallExpr) {
                    super.visitCallExpr(o)

                    val searchContext = SearchContext.get(o)
                    val prefixExpr = o.expr
                    var type = prefixExpr.guessType(searchContext)

                    if (type is TyUnion && type.size == 2 && type.getChildTypes().last().isAnonymous) {
                        type = type.getChildTypes().first()
                    }

                    var problemReported = false
                    val signatureMatch = type.matchSignature(searchContext, o) { _, sourceElement, message, highlightType ->
                        myHolder.registerProblem(sourceElement, message, highlightType)
                        problemReported = true
                    }

                    if (signatureMatch != null || problemReported) {
                        return
                    }

                    if (prefixExpr is LuaIndexExpr) {
                        // Get parent type
                        val parentType = prefixExpr.guessParentType(searchContext)

                        if (parentType is TyClass) {
                            val memberName = prefixExpr.name
                            val idExpr = prefixExpr.idExpr

                            if (memberName != null) {
                                val method = parentType.findSuperMember(memberName, searchContext)?.guessType(searchContext) as? ITyFunction

                                if (method != null) {
                                    method.matchSignature(searchContext, o, myHolder)
                                } else {
                                    myHolder.registerProblem(o, "Unknown function '$memberName'.")
                                }
                            } else if (idExpr != null) {
                                val indexTy = idExpr.guessType(searchContext)

                                TyUnion.each(indexTy) {
                                    val method = parentType.findIndexer(it, searchContext)?.guessType(searchContext) as? ITyFunction

                                    if (method != null) {
                                        method.matchSignature(searchContext, o, myHolder)
                                    } else {
                                        myHolder.registerProblem(o, "Unknown function '[${it.displayName}]'")
                                    }
                                }
                            }
                        }
                    } else if (type == Ty.NIL) {
                        myHolder.registerProblem(o, "Unknown function '%s'.".format(prefixExpr.lastChild.text))
                    }
                }
            }
}
