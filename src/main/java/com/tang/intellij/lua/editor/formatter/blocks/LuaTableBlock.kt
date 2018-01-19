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
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.tang.intellij.lua.editor.formatter.LuaFormatContext
import com.tang.intellij.lua.psi.LuaTableExpr
import com.tang.intellij.lua.psi.LuaTableField
import com.tang.intellij.lua.psi.LuaTypes

class LuaTableBlock(psi: LuaTableExpr, wrap: Wrap?, alignment: Alignment?, indent: Indent, ctx: LuaFormatContext)
    : LuaScriptBlock(psi, wrap, alignment, indent, ctx) {

    private val childAlign = Alignment.createAlignment()

    private val assignAlign:Alignment = Alignment.createAlignment(true)

    override fun buildChild(child: PsiElement, indent: Indent?): LuaScriptBlock {
        if (child is LuaTableField) {
            return LuaTableFieldBlock(assignAlign, child, null, childAlign, Indent.getNormalIndent(), ctx)
        } else if (child is PsiComment) {
            return LuaScriptBlock(child, null, null, Indent.getNormalIndent(), ctx)
        }
        return super.buildChild(child, indent)
    }

    override fun getChildAttributes(newChildIndex: Int) =
            ChildAttributes(Indent.getNormalIndent(), childAlign)
}

class LuaTableFieldBlock(private val assignAlign:Alignment, psi: LuaTableField, wrap: Wrap?, alignment: Alignment?, indent: Indent, ctx: LuaFormatContext)
    : LuaScriptBlock(psi, wrap, alignment, indent, ctx) {

    override fun buildChild(child: PsiElement, indent: Indent?): LuaScriptBlock {
        if (ctx.luaSettings.ALIGN_TABLE_FIELD_ASSIGN) {
            if (child.node.elementType == LuaTypes.ASSIGN) {
                return createBlock(child, Indent.getNoneIndent(), assignAlign)
            }
        }
        return super.buildChild(child, indent)
    }

}