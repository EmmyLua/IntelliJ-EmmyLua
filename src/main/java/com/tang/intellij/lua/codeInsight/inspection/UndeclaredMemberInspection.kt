/*
 * Copyright (c) 2020
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
import com.tang.intellij.lua.psi.LuaIndexExpr
import com.tang.intellij.lua.psi.LuaVisitor
import com.tang.intellij.lua.psi.prefixExpr
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.ITy
import com.tang.intellij.lua.ty.Ty

class UndeclaredMemberInspection : StrictInspection() {
    override fun buildVisitor(myHolder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
            object : LuaVisitor() {
                fun findMember(indexedType: ITy, memberName: String, context: SearchContext): Boolean {
                    var memberFound = false

                    indexedType.eachTopClass(Processor { topClass ->
                        memberFound = topClass.findMember(memberName, context) != null
                        !memberFound
                    })

                    return memberFound
                }

                override fun visitIndexExpr(o: LuaIndexExpr) {
                    val context = SearchContext.get(o)
                    val prefixType = o.prefixExpr.guessType(context)

                    if (prefixType != Ty.UNKNOWN) {
                        val memberName = o.id?.text

                        if (memberName != null && !findMember(prefixType, memberName, context)) {
                            myHolder.registerProblem(o.lastChild, "No such member '%s' found on type '%s'".format(memberName, prefixType))
                        }
                    }

                    super.visitIndexExpr(o)
                }
            }
}
