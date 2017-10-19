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

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.MacroCallNode
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.tang.intellij.lua.codeInsight.template.macro.SuggestTypeMacro
import com.tang.intellij.lua.psi.LuaCommentOwner
import com.tang.intellij.lua.psi.LuaFuncBodyOwner
import org.jetbrains.annotations.Nls


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