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
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.psi.LuaCallExpr
import com.tang.intellij.lua.psi.LuaElementFactory
import com.tang.intellij.lua.psi.LuaPsiTreeUtil
import com.tang.intellij.lua.psi.LuaTypes

class AppendCallParenIntention : BaseIntentionAction() {
    override fun getFamilyName() = "Append call paren"

    override fun getText() = familyName

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val callExpr = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaCallExpr::class.java, false) ?: return false
        return callExpr.args.node.findChildByType(LuaTypes.LPAREN) == null
    }

    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val callExpr = LuaPsiTreeUtil.findElementOfClassAtOffset(psiFile, editor.caretModel.offset, LuaCallExpr::class.java, false) ?: return
        val code = "${callExpr.expr.text}(${callExpr.args.text})"
        val file = LuaElementFactory.createFile(project, code)
        val newCall = PsiTreeUtil.findChildOfType(file, LuaCallExpr::class.java) ?: return
        callExpr.replace(newCall)
    }
}