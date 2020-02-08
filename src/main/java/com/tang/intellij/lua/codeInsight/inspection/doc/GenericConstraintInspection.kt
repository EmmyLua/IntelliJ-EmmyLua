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

package com.tang.intellij.lua.codeInsight.inspection.doc

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.tang.intellij.lua.comment.psi.LuaDocGeneralTy
import com.tang.intellij.lua.comment.psi.LuaDocGenericTy
import com.tang.intellij.lua.comment.psi.LuaDocType
import com.tang.intellij.lua.comment.psi.LuaDocVisitor
import com.tang.intellij.lua.psi.LuaPsiTreeUtil
import com.tang.intellij.lua.search.SearchContext

fun parameterText(count: Int): String {
    return if (count == 1) "parameter" else "parameters"
}

class GenericConstraintInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : LuaDocVisitor() {
            override fun visitType(o: LuaDocType) {
                if (o is LuaDocGenericTy) {
                    val context = SearchContext.get(o)
                    val params = LuaPsiTreeUtil.findClass(o.classNameRef.text, context)?.type?.params

                    if (params != null && params.size > 0) {
                        if (params.size != o.tyList.size) {
                            holder.registerProblem(o, "\"${o.classNameRef.text}\" requires ${params.size} generic ${parameterText(params.size)}", ProblemHighlightType.ERROR)
                        }

                        params.forEachIndexed { index, param ->
                            if (index < o.tyList.size) {
                                val valueType = o.tyList[index].getType()
                                if (!param.contravariantOf(valueType, context, 0)) {
                                    holder.registerProblem(o, "Type mismatch. Required: '%s' Found: '%s'".format(param, valueType))
                                }
                            }
                        }
                    } else if (o.classNameRef.text == "table") {
                        if (o.tyList.size != 2) {
                            holder.registerProblem(o, "table requires 2 generic parameters", ProblemHighlightType.ERROR)
                        }
                    } else {
                        holder.registerProblem(o, "\"${o.classNameRef.text}\" is not a generic type", ProblemHighlightType.ERROR)
                    }
                } else if (o is LuaDocGeneralTy) {
                    val context = SearchContext.get(o.project)
                    val params = LuaPsiTreeUtil.findClass(o.classNameRef.text, context)?.type?.params

                    if (params != null && params.size > 0) {
                        holder.registerProblem(o, "\"${o.classNameRef.text}\" requires ${params.size} generic ${parameterText(params.size)}", ProblemHighlightType.ERROR)
                    }
                }
            }
        }
    }
}
