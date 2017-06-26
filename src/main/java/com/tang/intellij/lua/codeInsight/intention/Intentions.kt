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
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.MacroCallNode
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.tang.intellij.lua.comment.psi.LuaDocReturnDef
import com.tang.intellij.lua.psi.*
import org.jetbrains.annotations.Nls

class CreateParameterAnnotationIntention : BaseIntentionAction() {
    @Nls
    override fun getFamilyName() = text

    override fun getText() = "Create parameter annotation"

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val offset = editor.caretModel.offset
        val name = findParamName(psiFile, offset) ?: findParamName(psiFile, offset - 1)
        return name != null && name.funcBodyOwner !is LuaClosureExpr
    }

    private fun findParamName(psiFile: PsiFile, offset:Int): LuaParamNameDef? {
        var element = psiFile.findElementAt(offset)
        if (element != null) {
            element = element.parent
            if (element is LuaParamNameDef) {
                //TODO: 并且没有相应 Doc
                return element
            }
        }
        return null
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val offset = editor.caretModel.offset
        val parDef = findParamName(psiFile, offset) ?: findParamName(psiFile, offset - 1)
        parDef ?: return

        val parametersOwner = parDef.owner
        if (parametersOwner is LuaCommentOwner) {
            val comment = parametersOwner.comment

            val templateManager = TemplateManager.getInstance(project)
            val template = templateManager.createTemplate("", "")
            if (comment != null) template.addTextSegment("\n")
            template.addTextSegment(String.format("---@param %s ", parDef.name))
            val name = MacroCallNode(SuggestTypeMacro())
            template.addVariable("type", name, TextExpression("table"), true)
            template.addEndVariable()

            if (comment != null) {
                editor.caretModel.moveToOffset(comment.textOffset + comment.textLength)
            } else {
                val commentOwner:LuaCommentOwner = parametersOwner
                editor.caretModel.moveToOffset(commentOwner.node.startOffset)
                template.addTextSegment("\n")
            }

            templateManager.startTemplate(editor, template)
        }
    }
}

class AppendCallParenIntention : BaseIntentionAction() {
    override fun getFamilyName() = "Append call paren"

    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val callExpr = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaCallExpr::class.java, false)
        if (callExpr != null) {
            val childByType = callExpr.args.node.findChildByType(LuaTypes.LPAREN)
            return childByType == null
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val callExpr = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaCallExpr::class.java, false)
        if (callExpr != null) {
            val argsNode = callExpr.args.node
            editor.document.insertString(argsNode.startOffset + argsNode.textLength, ")")
            editor.document.insertString(argsNode.startOffset, "(")

            PsiDocumentManager.getInstance(project).commitDocument(editor.document)
            CodeStyleManager.getInstance(project).reformat(callExpr)
        }
    }
}

class RemoveCallParenIntention : BaseIntentionAction() {
    override fun getFamilyName() = "Remove call paren"

    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val callExpr = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaCallExpr::class.java, false)
        if (callExpr != null) {
            callExpr.args.node.findChildByType(LuaTypes.LPAREN) ?: return false

            val exprList = callExpr.args.exprList
            if (exprList != null) {
                val list = exprList.exprList
                if (list.size != 1) return false
                val expr = list[0]
                when (expr.firstChild.node.elementType) {
                    LuaTypes.STRING -> return true
                    LuaTypes.TABLE_EXPR -> return true
                }
            }
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val callExpr = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaCallExpr::class.java, false)
        if (callExpr != null) {
            val argsNode = callExpr.args.node

            val rParen = argsNode.findChildByType(LuaTypes.RPAREN)
            if (rParen != null)
                argsNode.removeChild(rParen)

            val lParen = argsNode.findChildByType(LuaTypes.LPAREN)
            if (lParen != null)
                argsNode.removeChild(lParen)

            CodeStyleManager.getInstance(project).reformat(callExpr)
        }
    }
}

abstract class FunctionIntention : BaseIntentionAction() {
    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val bodyOwner = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaFuncBodyOwner::class.java, false)
        //不对Closure生效
        if (bodyOwner == null || bodyOwner is LuaClosureExpr)
            return false

        //不在body内
        val contains = bodyOwner.funcBody?.textRange?.contains(editor.caretModel.offset)
        if (contains != null && contains)
            return false

        return isAvailable(bodyOwner, editor)
    }

    abstract fun isAvailable(bodyOwner: LuaFuncBodyOwner, editor: Editor): Boolean

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val bodyOwner = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaFuncBodyOwner::class.java, false)
        if (bodyOwner != null) invoke(bodyOwner, editor)
    }

    abstract fun invoke(bodyOwner: LuaFuncBodyOwner, editor: Editor)
}

class CreateFunctionReturnAnnotationIntention : FunctionIntention() {
    override fun isAvailable(bodyOwner: LuaFuncBodyOwner, editor: Editor): Boolean {
        if (bodyOwner is LuaCommentOwner) {
            val comment = bodyOwner.comment
            return comment == null || PsiTreeUtil.getChildrenOfType(comment, LuaDocReturnDef::class.java) == null
        }
        return false
    }

    @Nls
    override fun getFamilyName() = text

    override fun getText() = "Create return annotation"

    override fun invoke(bodyOwner: LuaFuncBodyOwner, editor: Editor) {
        if (bodyOwner is LuaCommentOwner) {
            val comment = bodyOwner.comment
            val funcBody = bodyOwner.funcBody
            if (funcBody != null) {
                val templateManager = TemplateManager.getInstance(editor.project)
                val template = templateManager.createTemplate("", "")
                if (comment != null) template.addTextSegment("\n")
                template.addTextSegment("---@return ")
                val typeSuggest = MacroCallNode(SuggestTypeMacro())
                template.addVariable("returnType", typeSuggest, TextExpression("table"), false)
                template.addEndVariable()
                if (comment != null) {
                    editor.caretModel.moveToOffset(comment.textOffset + comment.textLength)
                } else {
                    template.addTextSegment("\n")
                    val e:PsiElement = bodyOwner
                    editor.caretModel.moveToOffset(e.node.startOffset)
                }
                templateManager.startTemplate(editor, template)
            }
        }
    }
}

class CreateFunctionDocIntention : FunctionIntention() {
    override fun isAvailable(bodyOwner: LuaFuncBodyOwner, editor: Editor): Boolean {
        if (bodyOwner is LuaCommentOwner) {
            return bodyOwner.comment == null || bodyOwner.funcBody == null
        }
        return false
    }

    @Nls
    override fun getFamilyName() = text

    override fun getText() = "Create LuaDoc"

    override fun invoke(bodyOwner: LuaFuncBodyOwner, editor: Editor) {
        val funcBody = bodyOwner.funcBody
        if (funcBody != null) {
            val templateManager = TemplateManager.getInstance(bodyOwner.project)
            val template = templateManager.createTemplate("", "")
            template.addTextSegment("---" + bodyOwner.name!!)
            val typeSuggest = MacroCallNode(SuggestTypeMacro())

            // params
            val parDefList = funcBody.paramNameDefList
            for (parDef in parDefList) {
                template.addTextSegment(String.format("\n---@param %s ", parDef.name))
                template.addVariable(parDef.name, typeSuggest, TextExpression("table"), false)
            }

            template.addEndVariable()
            template.addTextSegment("\n")

            val textOffset = bodyOwner.node.startOffset
            editor.caretModel.moveToOffset(textOffset)
            templateManager.startTemplate(editor, template)
        }
    }
}

/**
 * true <-> false
 */
class InvertBooleanIntention : BaseIntentionAction() {
    override fun getFamilyName() = text

    override fun getText() = "Invert boolean value"

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val element = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaLiteralExpr::class.java, false)
        if (element is LuaLiteralExpr && element.kind == LuaLiteralKind.Bool) {
            return true
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val element = PsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaLiteralExpr::class.java, false)
        if (element is LuaLiteralExpr && element.kind == LuaLiteralKind.Bool) {
            val lit = LuaElementFactory.createLiteral(project, if (element.text == "true") "false" else "true")
            element.replace(lit)
        }
    }

}

class InvertConditionIntention : BaseIntentionAction() {
    override fun getFamilyName() = text

    override fun getText() = "Invert condition"

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}