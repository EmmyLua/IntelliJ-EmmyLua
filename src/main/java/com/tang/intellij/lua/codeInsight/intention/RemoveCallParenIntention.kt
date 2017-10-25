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
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.tang.intellij.lua.psi.*

class RemoveCallParenIntention : BaseIntentionAction() {
    override fun getFamilyName() = "Remove call paren"

    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val callExpr = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaCallExpr::class.java, false)
        if (callExpr != null) {
            val args = callExpr.args as? LuaListArgs ?: return false
            val list = args.exprList
            if (list.isEmpty() || list.size > 1)
                return false
            val expr = list.first()
            when (expr) {
                is LuaTableExpr -> return true
                is LuaLiteralExpr -> return expr.kind == LuaLiteralKind.String
            }
        }
        return false
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val callExpr = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaCallExpr::class.java, false)
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