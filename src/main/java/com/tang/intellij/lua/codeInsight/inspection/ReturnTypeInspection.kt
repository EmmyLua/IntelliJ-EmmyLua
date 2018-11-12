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
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty
import com.tang.intellij.lua.ty.TyTuple

class ReturnTypeInspection : StrictInspection() {
    override fun buildVisitor(myHolder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
            object : LuaVisitor() {
                override fun visitReturnStat(o: LuaReturnStat) {
                    super.visitReturnStat(o)
                    if (o.parent is PsiFile)
                        return

                    val context = SearchContext(o.project)
                    val bodyOwner = PsiTreeUtil.getParentOfType(o, LuaFuncBodyOwner::class.java) ?: return
                    val abstractType = if (bodyOwner is LuaClassMethodDef) {
                        guessSuperReturnTypes(bodyOwner, context)
                    } else {
                        val returnDef = (bodyOwner as? LuaCommentOwner)?.comment?.tagReturn
                        returnDef?.type
                    }
                    val concreteType = guessReturnType(o, -1, context)
                    var abstractTypes = toList(abstractType)
                    val concreteTypes = toList(concreteType)

                    // Extend expected types with nil until the same amount as given types
                    if (abstractTypes.size < concreteTypes.size) {
                        abstractTypes += List(concreteTypes.size - abstractTypes.size) { Ty.UNKNOWN }
                    }

                    // Check number
                    if (abstractTypes.size > concreteTypes.size) {
                        if (concreteTypes.isEmpty()) {
                            myHolder.registerProblem(o.lastChild, "Type mismatch. Expected: '%s' Found: 'nil'".format(abstractTypes[0]))
                        } else {
                            myHolder.registerProblem(o.lastChild, "Incorrect number of return values. Expected %s but found %s.".format(abstractTypes.size, concreteTypes.size))
                        }
                    } else {
                        for (i in 0 until concreteTypes.size) {
                            if (!concreteTypes[i].subTypeOf(abstractTypes[i], context, false)) {
                                val abstractString = abstractTypes.joinToString(", ") { it.displayName }
                                val concreteString = concreteTypes.joinToString(", ") { it.displayName }
                                myHolder.registerProblem(o, "Type mismatch. Expected: '$abstractString' Found: '$concreteString'")
                                break
                            }
                        }
                    }
                }

                private fun toList(type: ITy?): List<ITy> {
                    return when (type) {
                        is TyTuple -> type.list
                        is ITy -> listOf(type)
                        else -> emptyList()
                    }
                }

                private fun guessSuperReturnTypes(function: LuaClassMethodDef, context: SearchContext): ITy? {
                    val comment = function.comment
                    if (comment != null) {
                        if (comment.isOverride()) {
                            // Find super type
                            val superClass = function.guessClassType(context)
                            val superMember = superClass?.findSuperMember(function.name ?: "", context)
                            if (superMember is LuaClassMethodDef) {
                                return superMember.guessReturnType(context)
                            }
                        } else {
                            return comment.tagReturn?.type
                        }
                    }
                    return null
                }

                override fun visitFuncBody(o: LuaFuncBody) {
                    super.visitFuncBody(o)

                    // If some return type is defined, we require at least one return type
                    val returnStat = PsiTreeUtil.findChildOfType(o, LuaReturnStat::class.java)

                    if (returnStat == null) {
                        // Find function definition
                        val context = SearchContext(o.project)
                        val bodyOwner = PsiTreeUtil.getParentOfType(o, LuaFuncBodyOwner::class.java)

                        val type = if (bodyOwner is LuaClassMethodDef) {
                            guessSuperReturnTypes(bodyOwner, context)
                        } else {
                            /*if (bodyOwner == null) {
                                myHolder.registerProblem(o, "Return statement needs to be in function.")
                            }*/
                            val returnDef = (bodyOwner as? LuaCommentOwner)?.comment?.tagReturn
                            returnDef?.type
                        }

                        val types = toList(type)

                        if (types.isNotEmpty()) {
                            myHolder.registerProblem(o, "Return type '%s' specified but no return values found.".format(types.joinToString(",")))
                        }
                    }
                }
            }
}