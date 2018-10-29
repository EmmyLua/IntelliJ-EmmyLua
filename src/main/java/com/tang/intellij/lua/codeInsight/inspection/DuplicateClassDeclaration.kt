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
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.tang.intellij.lua.LuaBundle
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.comment.psi.LuaDocVisitor
import com.tang.intellij.lua.stubs.index.LuaClassIndex

/**
 * 重复定义class
 * Created by TangZX on 2016/12/16.
 */
class DuplicateClassDeclaration : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : LuaDocVisitor() {
            override fun visitTagClass(o: LuaDocTagClass) {
                val useScope = o.useScope as? GlobalSearchScope ?: return
                val identifier = o.nameIdentifier
                LuaClassIndex.process(identifier.text, o.project, useScope, Processor {
                    if (it != o)
                        holder.registerProblem(identifier, LuaBundle.message("inspection.duplicate_class", it.containingFile.virtualFile.canonicalPath), ProblemHighlightType.GENERIC_ERROR)
                    true
                })
            }
        }
    }
}
