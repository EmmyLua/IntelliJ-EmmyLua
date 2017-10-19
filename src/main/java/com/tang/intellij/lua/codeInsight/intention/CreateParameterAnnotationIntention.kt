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

package com.tang.intellij.lua.codeInsight.intention

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInsight.template.impl.MacroCallNode
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.tang.intellij.lua.codeInsight.template.macro.SuggestTypeMacro
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.psi.LuaCommentOwner
import com.tang.intellij.lua.psi.LuaParamNameDef
import org.jetbrains.annotations.Nls

class CreateParameterAnnotationIntention : BaseIntentionAction() {
    @Nls
    override fun getFamilyName() = text

    override fun getText() = "Create parameter annotation"

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val offset = editor.caretModel.offset
        val name = findParamName(psiFile, offset) ?: findParamName(psiFile, offset - 1)
        return name != null //&& name.funcBodyOwner !is LuaClosureExpr
    }

    private fun findParamName(psiFile: PsiFile, offset:Int): LuaParamNameDef? {
        var element = psiFile.findElementAt(offset)
        if (element != null) {
            element = element.parent
            if (element is LuaParamNameDef) {
                val commentOwner = PsiTreeUtil.getParentOfType(element, LuaCommentOwner::class.java)
                val comment = commentOwner?.comment
                comment?.getParamDef(element.name) ?: return element
            }
        }
        return null
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val offset = editor.caretModel.offset
        val parDef = findParamName(psiFile, offset) ?: findParamName(psiFile, offset - 1)
        parDef ?: return

        val owner = PsiTreeUtil.getParentOfType(parDef, LuaCommentOwner::class.java)
        if (owner != null) {
            LuaCommentUtil.insertTemplate(owner, editor) { _, template ->
                template.addTextSegment(String.format("---@param %s ", parDef.name))
                val name = MacroCallNode(SuggestTypeMacro())
                template.addVariable("type", name, TextExpression("table"), true)
                template.addEndVariable()
            }
        }
    }
}