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
    override fun buildVisitor(myHolder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
            object : LuaVisitor() {
                override fun visitIndexExpr(o: LuaIndexExpr) {
                    super.visitIndexExpr(o)
                    val id = o.id
                    if (id != null) {
                        if (o.parent is LuaCallExpr && o.colon != null) {
                            // Guess parent types
                            val context = SearchContext(o.project)
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

                    val searchContext = SearchContext(o.project)
                    val type = o.expr.guessType(searchContext)

                    if (type is TyPsiFunction) {
                        val givenParams = o.args.children.filterIsInstance<LuaExpr>()
                        val givenTypes = givenParams.map { param -> param.guessType(searchContext) }

                        // Check if there are overloads?
                        if (type.signatures.isEmpty()) {
                            // Check main signature
                            if (!matchCallSignature(givenParams, givenTypes, type.mainSignature, searchContext)) {
                                annotateCall(o, givenParams, givenTypes, type.mainSignature.params, searchContext)
                            }
                        } else {
                            // Check if main signature matches
                            if (matchCallSignature(givenParams, givenTypes, type.mainSignature, searchContext)) return
                            // Check if there are other matching signatures
                            for (sig in type.signatures) {
                                if (matchCallSignature(givenParams, givenTypes, sig, searchContext)) return
                            }

                            // No matching overload found
                            val signatureString = givenTypes.joinToString(", ", transform = { t -> t.displayName })
                            val errorStr = "No matching overload of type: %s(%s)"
                            myHolder.registerProblem(o, errorStr.format(o.firstChild.text, signatureString))
                        }
                    } else if (o.expr is LuaIndexExpr) {
                        // Get parent type
                        val parentType = (o.expr as LuaIndexExpr).guessParentType(searchContext)
                        if (parentType is TyClass) {
                            val fType = parentType.findSuperMember(o.expr.name ?: "", searchContext)
                            if (fType == null) myHolder.registerProblem(o, "Unknown function '%s'.".format(o.expr.lastChild.text))
                        }
                    } else if (type == Ty.NIL) {
                        myHolder.registerProblem(o, "Unknown function '%s'.".format(o.expr.lastChild.text))
                    }
                }

                private fun annotateCall(call: LuaExpr, concreteParams: List<LuaExpr>, concreteTypes: List<ITy>, abstractParams: Array<LuaParamInfo>, searchContext: SearchContext) {
                    // Check if number of arguments match
                    if (concreteParams.size > abstractParams.size) {
                        val signatureString = abstractParams.joinToString(", ", transform = { param -> param.ty.displayName })
                        for (i in abstractParams.size until concreteParams.size) {
                            myHolder.registerProblem(concreteParams[i], "Too many arguments for type %s(%s).".format(call.firstChild.text, signatureString))
                        }
                    }
                    else if (concreteParams.size < abstractParams.size) {
                        for (i in concreteParams.size until abstractParams.size) {
                            myHolder.registerProblem(call.lastChild.lastChild, "Missing argument: %s: %s".format(abstractParams[i].name, abstractParams[i].ty.displayName))
                        }
                    }
                    else {
                        // Check individual arguments
                        for (i in 0 until concreteParams.size) {
                            // Check if concrete param is subtype of abstract type.
                            val concreteType = concreteTypes[i]
                            val abstractType = abstractParams[i].ty

                            if (!concreteType.subTypeOf(abstractType, searchContext)) {
                                myHolder.registerProblem(concreteParams[i], "Type mismatch. Required: '%s' Found: '%s'".format(abstractType.displayName, concreteType.displayName))
                            }
                        }
                    }
                }

                // Evaluate if concrete function parameters match abstract function parameters.
                private fun matchCallSignature(concreteParams: List<LuaExpr>, concreteTypes: List<ITy>, abstractParams: IFunSignature, searchContext: SearchContext): Boolean {
                    // Check if number of arguments matches
                    if (concreteParams.size != abstractParams.params.size) return false

                    // Check individual arguments
                    for (i in 0 until concreteParams.size) {
                        // Check if concrete param is subtype of abstract type.
                        val concreteType = concreteTypes[i]
                        val abstractType = abstractParams.params[i].ty

                        if (!concreteType.subTypeOf(abstractType, searchContext)) {
                            return false
                        }
                    }

                    return true
                }
            }
}