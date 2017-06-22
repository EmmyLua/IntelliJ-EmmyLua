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
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.LuaCallExpr
import com.tang.intellij.lua.psi.LuaTypes

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