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

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.search.SearchContext

class GlobalNameCanbeLocal : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : LuaVisitor() {
            override fun visitNameExpr(o: LuaNameExpr) {
                val stat = o.assignStat
                if (stat != null && o.moduleName == null) {
                    val name = o.name
                    val resolve = resolveInFile(name, o, SearchContext(o.project))
                    if (resolve == null) {
                        val scope = GlobalSearchScope.allScope(o.project)
                        val searchScope = scope.intersectWith(GlobalSearchScope.notScope(GlobalSearchScope.fileScope(o.containingFile)))
                        val query = ReferencesSearch.search(o, searchScope)
                        var canLocal = query.findFirst() == null
                        if (canLocal) {
                            canLocal = o.reference?.resolve() == null
                        }
                        if (canLocal) {
                            holder.registerProblem(o, "Global name \"$name\" can be local", object : LocalQuickFix {
                                override fun getFamilyName() = "Append \"local\""

                                override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                                    val element = LuaElementFactory.createWith(project, "local ${stat.text}")
                                    stat.replace(element)
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}