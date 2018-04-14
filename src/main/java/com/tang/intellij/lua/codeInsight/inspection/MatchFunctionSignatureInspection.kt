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
import com.intellij.util.Processor
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
                    val prefixExpr = o.expr
                    val type = prefixExpr.guessType(searchContext)

                    if (type is TyPsiFunction) {
                        val givenParams = o.argList
                        val givenTypes = mutableListOf<ITy>()
                        for ((i, param) in givenParams.withIndex()) {
                            val paramType = param.guessType(searchContext)
                            if (paramType is TyTuple) {
                                if (i == givenParams.lastIndex) {
                                    givenTypes.addAll(paramType.list)
                                }
                                else {
                                    givenTypes.add(paramType.list.first())
                                }
                            }
                            else {
                                givenTypes.add(paramType)
                            }
                        }
                        //find the perfect one.
                        var perfectSig: IFunSignature? = null
                        var perfectMatchNum = -1
                        var fullMatch = false
                        type.process(Processor { sig ->
                            val pair = matchCallSignature(givenTypes, sig, searchContext)
                            if (pair.second > perfectMatchNum) {
                                perfectSig = sig
                                perfectMatchNum = pair.second
                            }
                            if (pair.first)
                                fullMatch = true
                            !fullMatch
                        })
                        if (!fullMatch && perfectSig != null) {
                            annotateCall(o, givenParams, givenTypes, perfectSig!!, searchContext)
                        }
                    } else if (prefixExpr is LuaIndexExpr) {
                        // Get parent type
                        val parentType = prefixExpr.guessParentType(searchContext)
                        if (parentType is TyClass) {
                            val fType = prefixExpr.name?.let { parentType.findSuperMember(it, searchContext) }
                            if (fType == null)
                                myHolder.registerProblem(o, "Unknown function '%s'.".format(prefixExpr.lastChild.text))
                        }
                    } else if (type == Ty.NIL) {
                        myHolder.registerProblem(o, "Unknown function '%s'.".format(prefixExpr.lastChild.text))
                    }
                }

                private fun annotateCall(call: LuaCallExpr, concreteParams: List<LuaExpr>, concreteTypes: List<ITy>, signature: IFunSignature, searchContext: SearchContext) {
                    val abstractParams = signature.params
                    val hasVarArgs = signature.hasVarArgs()
                    val sigParamSize = if (hasVarArgs) signature.params.size - 1 else signature.params.size

                    // Check if number of arguments match
                    // 代码明确填写的实参过多
                    if (concreteParams.size > sigParamSize) {
                        if (!hasVarArgs) {
                            val signatureString = abstractParams.joinToString(", ", transform = { param -> param.ty.displayName })
                            for (i in sigParamSize until concreteParams.size) {
                                myHolder.registerProblem(concreteParams[i], "Too many arguments for type %s(%s).".format(call.firstChild.text, signatureString))
                            }
                        }
                    }
                    // 实参过少
                    else if (concreteTypes.size < sigParamSize) {
                        for (i in concreteTypes.size until sigParamSize) {
                            myHolder.registerProblem(call.lastChild.lastChild, "Missing argument: %s: %s".format(abstractParams[i].name, abstractParams[i].ty.displayName))
                        }
                    }
                    else {
                        // Check individual arguments
                        for (i in 0 until sigParamSize) {
                            // Check if concrete param is subtype of abstract type.
                            val concreteType = concreteTypes[i]
                            val abstractType = abstractParams[i].ty

                            if (!concreteType.subTypeOf(abstractType, searchContext, false)) {
                                myHolder.registerProblem(concreteParams[i], "Type mismatch. Required: '%s' Found: '%s'".format(abstractType.displayName, concreteType.displayName))
                            }
                        }
                    }
                }

                // Evaluate if concrete function parameters match abstract function parameters.
                private fun matchCallSignature(concreteTypes: List<ITy>, signature: IFunSignature, searchContext: SearchContext): Pair<Boolean, Int> {
                    val hasVarArgs = signature.hasVarArgs()
                    val sigParamSize = if (hasVarArgs) signature.params.size - 1 else signature.params.size

                    // Check if number of arguments matches
                    if (hasVarArgs) {
                        if (concreteTypes.size < sigParamSize)
                            return Pair(false, 0)
                    } else if (concreteTypes.size != sigParamSize)
                        return Pair(false, 0)

                    var matchScore = 0
                    // Check individual arguments
                    for (i in 0 until sigParamSize) {
                        // Check if concrete param is subtype of abstract type.
                        val concreteType = concreteTypes[i]
                        val abstractType = signature.getParamTy(i)

                        if (concreteType.subTypeOf(abstractType, searchContext, false)) {
                            matchScore++
                        }
                    }

                    return Pair(matchScore == sigParamSize, matchScore)
                }
            }
}