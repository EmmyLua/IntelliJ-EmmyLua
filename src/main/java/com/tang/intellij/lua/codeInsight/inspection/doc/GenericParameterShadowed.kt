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

package com.tang.intellij.lua.codeInsight.inspection.doc

import com.intellij.codeInspection.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringActionHandlerFactory
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocGenericDef
import com.tang.intellij.lua.comment.psi.LuaDocVisitor
import com.tang.intellij.lua.psi.LuaCommentOwner

class GenericParameterShadowed : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : LuaDocVisitor() {
            override fun visitGenericDef(o: LuaDocGenericDef) {
                super.visitGenericDef(o)

                val genericName = o.id.text
                val container = LuaCommentUtil.findContainer(o)
                var psiElement = container.parent

                while (psiElement != null) {
                    val commentOwner = PsiTreeUtil.getParentOfType(psiElement, LuaCommentOwner::class.java)
                    val comment = commentOwner?.comment

                    if (comment != null) {
                        val genericDefs = comment.findTags(LuaDocGenericDef::class.java)

                        for (def in genericDefs) {
                            if (def.id.text == genericName) {
                                val document = FileDocumentManager.getInstance().getDocument(def.containingFile.virtualFile)
                                val desc = if (document != null) {
                                    "Generic parameters cannot be shadowed, '$genericName' was previously defined on line ${document.getLineNumber(def.node.startOffset) + 1}"
                                } else {
                                    "Generic parameters cannot be shadowed, '$genericName' was previously defined."
                                }

                                holder.registerProblem(o, desc, ProblemHighlightType.ERROR, object : RefactoringQuickFix {
                                    override fun getHandler(): RefactoringActionHandler {
                                        return RefactoringActionHandlerFactory.getInstance().createRenameHandler()
                                    }

                                    override fun getFamilyName(): String {
                                        return "Rename"
                                    }
                                })

                                return
                            }
                        }
                    }

                    psiElement = commentOwner?.parent
                }
            }
        }
    }
}
