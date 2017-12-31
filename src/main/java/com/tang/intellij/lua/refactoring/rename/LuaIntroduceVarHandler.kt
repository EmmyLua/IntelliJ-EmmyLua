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

package com.tang.intellij.lua.refactoring.rename

import com.intellij.codeInsight.CodeInsightUtilCore
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.introduce.inplace.InplaceVariableIntroducer
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.refactoring.LuaRefactoringUtil
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 *
 * Created by tangzx on 2017/4/25.
 */
class LuaIntroduceVarHandler : RefactoringActionHandler {

    internal inner class IntroduceOperation(val element: PsiElement, val project: Project, val editor: Editor, val file: PsiFile) {
        var isReplaceAll: Boolean = false
        var occurrences: List<PsiElement>? = null
        var name = "var"
        var newOccurrences: List<PsiElement>? = null
        var newNameElement: LuaNameDef? = null
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile, dataContext: DataContext) {

    }

    override fun invoke(project: Project, psiElements: Array<PsiElement>, dataContext: DataContext) {

    }

    operator fun invoke(project: Project, editor: Editor, expr: LuaExpr) {
        val operation = IntroduceOperation(expr, project, editor, expr.containingFile)
        operation.occurrences = getOccurrences(expr)
        OccurrencesChooser.simpleChooser<PsiElement>(editor).showChooser(expr, operation.occurrences!!, object : Pass<OccurrencesChooser.ReplaceChoice>() {
            override fun pass(choice: OccurrencesChooser.ReplaceChoice) {
                operation.isReplaceAll = choice == OccurrencesChooser.ReplaceChoice.ALL
                WriteCommandAction.runWriteCommandAction(operation.project) { performReplace(operation) }
                performInplaceIntroduce(operation)
            }
        })
    }

    private fun getOccurrences(expr: LuaExpr): List<PsiElement> {
        return LuaRefactoringUtil.getOccurrences(expr, expr.containingFile)
    }

    private fun findAnchor(occurrences: List<PsiElement>?): PsiElement? {
        var anchor = occurrences!![0]
        next@ do {
            val statement = PsiTreeUtil.getParentOfType(anchor, LuaStatement::class.java)
            if (statement != null) {
                val parent = statement.parent
                for (element in occurrences) {
                    if (!PsiTreeUtil.isAncestor(parent, element, true)) {
                        anchor = statement
                        continue@next
                    }
                }
            }
            return statement
        } while (true)
    }

    private fun isInline(commonParent: PsiElement, operation: IntroduceOperation): Boolean {
        var parent = commonParent
        if (parent === operation.element)
            parent = operation.element.parent
        return parent is LuaStatement && (!operation.isReplaceAll || operation.occurrences!!.size == 1)
    }

    private fun performReplace(operation: IntroduceOperation) {
        if (!operation.isReplaceAll)
            operation.occurrences = listOf(operation.element)

        var commonParent = PsiTreeUtil.findCommonParent(operation.occurrences!!)
        if (commonParent != null) {
            var element = operation.element
            val newOccurrences = SmartList<PsiElement>()
            var localDef = LuaElementFactory.createWith(operation.project, "local var = " + element.text)

            if (isInline(commonParent, operation)) {
                if (element is LuaCallExpr && element.parent is LuaExprStat) element = element.parent

                localDef = element.replace(localDef)
                val nameDef = PsiTreeUtil.findChildOfType(localDef, LuaNameDef::class.java)!!
                operation.editor.caretModel.moveToOffset(nameDef.textOffset)
            } else {
                val anchor = findAnchor(operation.occurrences)
                commonParent = anchor!!.parent
                localDef = commonParent!!.addBefore(localDef, anchor)
                commonParent.addAfter(LuaElementFactory.newLine(operation.project), localDef)
                for (occ in operation.occurrences!!) {
                    var identifier = LuaElementFactory.createName(operation.project, operation.name)
                    identifier = occ.replace(identifier)
                    newOccurrences.add(identifier)
                }
            }

            operation.newOccurrences = newOccurrences
            localDef = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(localDef)
            val nameDef = PsiTreeUtil.findChildOfType(localDef, LuaNameDef::class.java)
            operation.newNameElement = nameDef
        }
    }

    private fun performInplaceIntroduce(operation: IntroduceOperation) {
        LuaIntroduce(operation).performInplaceRefactoring(null)
    }

    private inner class LuaIntroduce internal constructor(operation: IntroduceOperation)
        : InplaceVariableIntroducer<PsiElement>(operation.newNameElement, operation.editor, operation.project, "Introduce Variable", operation.newOccurrences?.toTypedArray(), null) {

        override fun checkLocalScope(): PsiElement? {
            val currentFile = PsiDocumentManager.getInstance(this.myProject).getPsiFile(this.myEditor.document)
            return currentFile ?: super.checkLocalScope()
        }
    }
}
