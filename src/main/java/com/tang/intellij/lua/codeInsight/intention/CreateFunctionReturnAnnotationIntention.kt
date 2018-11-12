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

import com.intellij.codeInsight.template.impl.MacroCallNode
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.psi.util.PsiTreeUtil
import com.tang.intellij.lua.codeInsight.template.macro.SuggestTypeMacro
import com.tang.intellij.lua.comment.LuaCommentUtil
import com.tang.intellij.lua.comment.psi.LuaDocTagReturn
import com.tang.intellij.lua.psi.LuaCommentOwner
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import org.jetbrains.annotations.Nls


class CreateFunctionReturnAnnotationIntention : FunctionIntention() {
    override fun isAvailable(bodyOwner: LuaFuncBodyOwner, editor: Editor): Boolean {
        if (bodyOwner is LuaCommentOwner) {
            val comment = bodyOwner.comment
            return comment == null || PsiTreeUtil.getChildrenOfType(comment, LuaDocTagReturn::class.java) == null
        }
        return false
    }

    @Nls
    override fun getFamilyName() = text

    override fun getText() = "Create return annotation"

    override fun invoke(bodyOwner: LuaFuncBodyOwner, editor: Editor) {
        if (bodyOwner is LuaCommentOwner) {
            LuaCommentUtil.insertTemplate(bodyOwner, editor) { _, template ->
                template.addTextSegment("---@return ")
                val typeSuggest = MacroCallNode(SuggestTypeMacro())
                template.addVariable("returnType", typeSuggest, TextExpression("table"), false)
                template.addEndVariable()
            }
        }
    }
}