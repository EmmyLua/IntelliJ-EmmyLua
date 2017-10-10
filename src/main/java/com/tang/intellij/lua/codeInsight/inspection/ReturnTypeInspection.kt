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
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

class ReturnTypeInspection : StrictInspection() {
    override fun buildVisitor(myHolder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
            object : LuaVisitor() {
                override fun visitReturnStat(o: LuaReturnStat) {
                    super.visitReturnStat(o)

                    val context = SearchContext(o.project)

                    val function = PsiTreeUtil.getParentOfType(o, LuaClassMethodDef::class.java)
                    var abstractTypes = if (function != null) {
                        guessSuperReturnTypes(function, context)
                    } else {
                        val fdef = PsiTreeUtil.getParentOfType(o, LuaFuncDef::class.java)
                        if (fdef == null) {
                            myHolder.registerProblem(o, "Return statement needs to be in function.")
                        }
                        val comment = fdef?.comment?.returnDef
                        comment?.typeList?.tyList?.map { it.getType() } ?: listOf()
                    }

                    val concreteValues = o.exprList?.exprList ?: listOf()
                    val concreteTypes = concreteValues.map { expr -> expr.guessType(context) }

                    // Extend expected types with nil until the same amount as given types
                    if (abstractTypes.size < concreteTypes.size) {
                        abstractTypes += List(concreteTypes.size - abstractTypes.size, { Ty.NIL })
                    }

                    // Check number
                    if (abstractTypes.size > concreteTypes.size) {
                        if (concreteTypes.isEmpty()) {
                            myHolder.registerProblem(o.lastChild, "Type mismatch. Expected: '%s' Found: 'nil'".format(abstractTypes[0]))
                        } else {
                            myHolder.registerProblem(o.lastChild, "Incorrect number of return values. Expected %s but found %s.".format(abstractTypes.size, concreteTypes.size))
                        }
                    } else {
                        for (i in 0 until concreteValues.size) {
                            if (!concreteTypes[i].subTypeOf(abstractTypes[i], context)) {
                                myHolder.registerProblem(concreteValues[i], "Type mismatch. Expected: '%s' Found: '%s'".format(abstractTypes[i], concreteTypes[i]))
                            }
                        }
                    }
                }

                private fun guessSuperReturnTypes(function: LuaClassMethodDef, context: SearchContext): List<ITy> {
                    var types = listOf<ITy>()
                    val comment = function.comment
                    if (comment != null) {
                        if (comment.isOverride()) {
                            // Find super type
                            val superClass = function.guessClassType(context)
                            val superMember = superClass?.findSuperMember(function.name ?: "", context)
                            if (superMember is LuaClassMethodDef) {
                                val typeDef = superMember.comment?.returnDef?.typeList?.tyList ?: listOf()
                                types = typeDef.map { ty -> ty.getType() }
                            }
                        } else {
                            val funcTypes = comment.returnDef?.typeList?.tyList ?: listOf()
                            types = funcTypes.map { ty -> ty.getType() }
                        }
                    }
                    return types
                }

                override fun visitFuncBody(o: LuaFuncBody) {
                    super.visitFuncBody(o)

                    // Ignore empty functions -- Definitions
                    if (o.children.size < 2) return
                    if (o.children[1].children.isEmpty()) return

                    // Find function definition
                    val context = SearchContext(o.project)
                    val funcDef = PsiTreeUtil.getParentOfType(o, LuaClassMethodDef::class.java)

                    val types = if (funcDef != null) {
                        guessSuperReturnTypes(funcDef, context)
                    } else {
                        val fdef = PsiTreeUtil.getParentOfType(o, LuaFuncDef::class.java)
                        if (fdef == null) {
                            myHolder.registerProblem(o, "Return statement needs to be in function.")
                        }
                        val comment = fdef?.comment?.returnDef
                        comment?.typeList?.tyList?.map { it.getType() } ?: listOf()
                    }

                    // If some return type is defined, we require at least one return type
                    val returns = PsiTreeUtil.findChildOfType(o, LuaReturnStat::class.java)

                    if (!types.isEmpty() && types[0] != Ty.NIL && returns == null) {
                        myHolder.registerProblem(o, "Return type '%s' specified but no return values found.".format(types.joinToString(",")))
                    }
                }
            }
}