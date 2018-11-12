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
                        val perfectSig = type.findPerfectSignature(o)
                        annotateCall(o, perfectSig, searchContext)
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

                private fun annotateCall(call: LuaCallExpr, signature: IFunSignature, searchContext: SearchContext) {
                    val concreteParams = call.argList
                    val concreteTypes = mutableListOf<ConcreteTypeInfo>()
                    concreteParams.forEachIndexed { index, luaExpr ->
                        val ty = luaExpr.guessType(searchContext)
                        if (ty is TyTuple) {
                            if (index == concreteParams.lastIndex) {
                                concreteTypes.addAll(ty.list.map { ConcreteTypeInfo(luaExpr, it) })
                            } else {
                                concreteTypes.add(ConcreteTypeInfo(luaExpr, ty.list.first()))
                            }
                        } else concreteTypes.add(ConcreteTypeInfo(luaExpr, ty))
                    }

                    var nArgs = 0
                    signature.processArgs(call) { i, pi ->
                        nArgs = i + 1
                        val typeInfo = concreteTypes.getOrNull(i)
                        if (typeInfo == null) {
                            myHolder.registerProblem(call.lastChild.lastChild, "Missing argument: ${pi.name}: ${pi.ty}")
                            return@processArgs true
                        }

                        val type = typeInfo.ty
                        if (!type.subTypeOf(pi.ty, searchContext, false))
                            myHolder.registerProblem(typeInfo.param, "Type mismatch. Required: '${pi.ty}' Found: '$type'")
                        true
                    }
                    if (nArgs < concreteParams.size && !signature.hasVarargs()) {
                        for (i in nArgs until concreteParams.size) {
                            myHolder.registerProblem(concreteParams[i], "Too many arguments.")
                        }
                    }
                }
            }
}