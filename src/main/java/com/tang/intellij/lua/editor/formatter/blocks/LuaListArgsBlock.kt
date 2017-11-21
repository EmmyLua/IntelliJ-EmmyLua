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

package com.tang.intellij.lua.editor.formatter.blocks

import com.intellij.formatting.Alignment
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.editor.formatter.LuaFormatContext
import com.tang.intellij.lua.psi.LuaExpr
import com.tang.intellij.lua.psi.LuaListArgs

class LuaListArgsBlock(psi: LuaListArgs, wrap: Wrap?, alignment: Alignment?, indent: Indent, ctx: LuaFormatContext)
    : LuaScriptBlock(psi, wrap, alignment, indent, ctx) {

    private val argAlign = Alignment.createAlignment()
    private var argIndex = 0

    private fun getAlign()
            = if (ctx.settings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS) argAlign else null

    override fun buildChild(child: PsiElement, indent: Indent?): LuaScriptBlock {
        if (child is LuaExpr) {
            val wrap = if (argIndex++ == 0) null else
                Wrap.createWrap(ctx.settings.CALL_PARAMETERS_WRAP, true)
            return createBlock(child, Indent.getContinuationIndent(), getAlign(), wrap)
        }
        return super.buildChild(child, indent)
    }

    override fun getChildAttributes(newChildIndex: Int) =
            ChildAttributes(Indent.getContinuationIndent(), getAlign())
}