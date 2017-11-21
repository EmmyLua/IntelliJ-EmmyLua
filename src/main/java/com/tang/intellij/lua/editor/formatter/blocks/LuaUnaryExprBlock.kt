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
import com.tang.intellij.lua.editor.formatter.LuaFormatContext
import com.tang.intellij.lua.psi.LuaTypes.NOT
import com.tang.intellij.lua.psi.LuaUnaryExpr

/**
 *
 * Created by tangzx on 2017/4/23.
 */
class LuaUnaryExprBlock internal constructor(psi: LuaUnaryExpr,
                                             wrap: Wrap?,
                                             alignment: Alignment?,
                                             indent: Indent,
                                             ctx: LuaFormatContext)
    : LuaScriptBlock(psi, wrap, alignment, indent, ctx) {

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        val c1 = child1 as LuaScriptBlock
        return if (c1.node.findChildByType(NOT) != null) Spacing.createSpacing(1, 1, 0, true, 1) else super.getSpacing(child1, child2)
    }
}
