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
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.editor.formatter.LuaFormatContext
import com.tang.intellij.lua.psi.LuaTypes

class LuaAssignBlock(psi: PsiElement, wrap: Wrap?, alignment: Alignment?, indent: Indent, ctx: LuaFormatContext)
    : LuaScriptBlock(psi, wrap, alignment, indent, ctx)  {

    private val _assignAlign = Alignment.createAlignment(true)

    private val assignAlign:Alignment? get() {
        val prev = getPrevSkipComment()
        return if (prev is LuaAssignBlock) prev.assignAlign else _assignAlign
    }

    override fun buildChild(child: PsiElement, indent: Indent?): LuaScriptBlock {
        if (ctx.settings.ALIGN_CONSECUTIVE_VARIABLE_DECLARATIONS && child.node.elementType == LuaTypes.ASSIGN) {
            return createBlock(child, Indent.getContinuationIndent(), assignAlign)
        }
        return super.buildChild(child, indent)
    }
}