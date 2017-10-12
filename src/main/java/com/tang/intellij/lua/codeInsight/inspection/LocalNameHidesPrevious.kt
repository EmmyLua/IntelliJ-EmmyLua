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
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.RefactoringQuickFix
import com.intellij.psi.PsiElementVisitor
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringActionHandlerFactory
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.psi.LuaLocalDef
import com.tang.intellij.lua.psi.LuaPsiTreeUtil
import com.tang.intellij.lua.psi.LuaVisitor

class LocalNameHidesPrevious : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : LuaVisitor() {
            override fun visitLocalDef(o: LuaLocalDef) {
                o.nameList?.nameDefList?.forEach {
                    val name = it.name
                    if (name != Constants.WORD_UNDERLINE) {
                        LuaPsiTreeUtil.walkUpLocalNameDef(it) { nameDef ->
                            if (it.name == nameDef.name) {
                                holder.registerProblem(it, "Local name hides previous", object : RefactoringQuickFix {
                                    override fun getHandler(): RefactoringActionHandler {
                                        return RefactoringActionHandlerFactory.getInstance().createRenameHandler()
                                    }

                                    override fun getFamilyName(): String {
                                        return "Rename"
                                    }
                                })
                                return@walkUpLocalNameDef false
                            }
                            return@walkUpLocalNameDef true
                        }
                    }
                }
                super.visitLocalDef(o)
            }
        }
    }
}