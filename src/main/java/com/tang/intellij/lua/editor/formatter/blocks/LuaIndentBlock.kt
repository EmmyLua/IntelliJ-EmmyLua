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

import com.intellij.formatting.*
import com.intellij.psi.tree.TokenSet
import com.tang.intellij.lua.editor.formatter.LuaFormatContext
import com.tang.intellij.lua.psi.LuaIndentRange
import com.tang.intellij.lua.psi.LuaTypes.*

open class LuaIndentBlock(psi: LuaIndentRange, wrap: Wrap?, alignment: Alignment?, indent: Indent, ctx: LuaFormatContext)
    : LuaScriptBlock(psi, wrap, alignment, indent, ctx)  {

    private val set = TokenSet.create(
            DO, END, IF, ELSE, ELSEIF, WHILE, THEN, RPAREN
    )

    private val space = Spacing.createSpacing(1, 1, 0, true, 1)

    private fun isSimpleBlock(current: LuaScriptBlock, getNext: (c:LuaScriptBlock) -> LuaScriptBlock?): Boolean {
        val next = getNext(current)
        if (next == null || set.contains(next.elementType))
            return true
        val nn = getNext(next)
        if (nn != null && set.contains(nn.elementType)) {
            return true
        }
        return false
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        if (ctx.settings.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE) {
            if (child2 is LuaScriptBlock) {
                if (set.contains(child2.elementType) && isSimpleBlock(child2) { it.prevBlock }) {
                    return space
                }
            }
            if (child1 is LuaScriptBlock) {
                if (set.contains(child1.elementType) && isSimpleBlock(child1) { it.nextBlock }) {
                    return space
                }
            }
        }

        return super.getSpacing(child1, child2)
    }
}